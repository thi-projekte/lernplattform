import { useCallback, useEffect, useRef, useState } from 'react';
import keycloak from '../keycloak.ts';
import { ChatMessageDtoSchema, type ChatMessageDto } from '../schemas/chat.ts';

export type ChatStatus = 'connecting' | 'open' | 'closed' | 'error';

const MAX_RECONNECTS = 5;

const buildWsUrl = (topicId: string) => {
  const base = import.meta.env.VITE_MYND_BACKEND_URI as string;
  // http -> ws, https -> wss
  return `${base.replace(/^http/, 'ws')}/topics/${topicId}/websocket-chat`;
};

/**
 * Manages the per-topic chat WebSocket: connects with the Keycloak bearer token
 * passed in the sub-protocol slot, exposes live messages (deduplicated by id),
 * a send function and the connection status. History is merged in via seedHistory.
 */
export const useTopicChat = (topicId: string | undefined) => {
  const [messages, setMessages] = useState<ChatMessageDto[]>([]);
  const [status, setStatus] = useState<ChatStatus>('connecting');
  const wsRef = useRef<WebSocket | null>(null);
  const reconnectsRef = useRef(0);
  const reconnectTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const appendMessage = useCallback((msg: ChatMessageDto) => {
    setMessages((prev) => (prev.some((m) => m.id === msg.id) ? prev : [...prev, msg]));
  }, []);

  // Prepends older history messages that are not already present (live messages
  // arrive after the connection opens and stay at the end).
  const seedHistory = useCallback((history: ChatMessageDto[]) => {
    setMessages((prev) => {
      const seen = new Set(prev.map((m) => m.id));
      const older = history.filter((m) => !seen.has(m.id));
      return older.length ? [...older, ...prev] : prev;
    });
  }, []);

  useEffect(() => {
    if (!topicId) return;
    let cancelled = false;

    const connect = async () => {
      try {
        // Refresh only when actually expired. Calling updateToken
        // unconditionally — twice under React StrictMode — can leave the second
        // promise unresolved and hang the connection. Mirror the apiClient's
        // guarded refresh.
        if (keycloak.isTokenExpired()) {
          console.warn('[chat] token expired, refreshing…');
          await keycloak.updateToken(30);
        }
        console.warn('[chat] token ready, len=', keycloak.token?.length);
      } catch (e) {
        console.warn('[chat] token refresh failed', e);
        // Proceed with the current token; the handshake fails if it is invalid.
      }
      if (cancelled) return;
      setStatus('connecting');

      const url = buildWsUrl(topicId);
      console.warn('[chat] opening', url);
      // Quarkus WebSockets Next bearer-token contract: the second subprotocol is
      // not the raw token but an encoded "quarkus-http-upgrade#<header>#<value>"
      // directive that the server (with propagate-subprotocol-headers=true) maps
      // to a handshake request header. The first entry is the carrier listed in
      // supported-subprotocols.
      const authProtocol = encodeURIComponent(
        `quarkus-http-upgrade#Authorization#Bearer ${keycloak.token ?? ''}`
      );
      const ws = new WebSocket(url, ['bearer-token-carrier', authProtocol]);
      wsRef.current = ws;

      ws.onopen = () => {
        console.warn('[chat] OPEN, protocol=', ws.protocol);
        reconnectsRef.current = 0;
        setStatus('open');
      };
      ws.onmessage = (event) => {
        try {
          const parsed = ChatMessageDtoSchema.safeParse(JSON.parse(event.data as string));
          if (parsed.success) appendMessage(parsed.data);
        } catch {
          // Ignore non-JSON frames (e.g. the plain "Access denied" error text).
        }
      };
      ws.onerror = (event) => {
        console.warn('[chat] ERROR', event);
        setStatus('error');
      };
      ws.onclose = (event) => {
        console.warn(
          '[chat] CLOSE code=',
          event.code,
          'reason=',
          event.reason,
          'clean=',
          event.wasClean
        );
        if (cancelled) return;
        setStatus('closed');
        // 1008 = policy violation (auth failure) — retrying will not help.
        if (event.code !== 1008 && reconnectsRef.current < MAX_RECONNECTS) {
          const delay = Math.min(1000 * 2 ** reconnectsRef.current, 10000);
          reconnectsRef.current += 1;
          reconnectTimerRef.current = setTimeout(() => void connect(), delay);
        }
      };
    };

    void connect();

    return () => {
      cancelled = true;
      if (reconnectTimerRef.current) clearTimeout(reconnectTimerRef.current);
      wsRef.current?.close();
      wsRef.current = null;
    };
  }, [topicId, appendMessage]);

  const sendMessage = useCallback((text: string) => {
    const trimmed = text.trim();
    if (!trimmed || wsRef.current?.readyState !== WebSocket.OPEN) return false;
    wsRef.current.send(JSON.stringify({ message: trimmed }));
    return true;
  }, []);

  return { messages, status, sendMessage, seedHistory };
};
