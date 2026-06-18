import { RichTextEditor, Link } from '@mantine/tiptap';
import { useEditor } from '@tiptap/react';
import StarterKit from '@tiptap/starter-kit';

const normalizeHref = (href: string): string => {
  const trimmed = href.trim();
  if (!trimmed) return trimmed;
  if (/^(#|\/|\.{1,2}\/)/.test(trimmed)) return trimmed;
  if (/^[a-z][a-z0-9+.-]*:/i.test(trimmed)) return trimmed;
  return `https://${trimmed}`;
};

const SafeLink = Link.extend({
  addAttributes() {
    const parent = this.parent?.() ?? {};
    const hrefAttr = (parent as { href?: object }).href ?? {};
    return {
      ...parent,
      href: {
        ...hrefAttr,
        renderHTML: (attrs: { href?: string | null }) =>
          attrs.href ? { href: normalizeHref(attrs.href) } : {},
      },
    };
  },
}).configure({ defaultProtocol: 'https' });

interface RtfEditorProps {
  value?: string;
  onChange: (content: string) => void;
}

const RtfEditor = ({ value, onChange }: RtfEditorProps) => {
  const editor = useEditor({
    extensions: [StarterKit, SafeLink],
    content: value,
    onUpdate({ editor }) {
      onChange(editor.getHTML());
    },
  });

  return (
    <RichTextEditor editor={editor}>
      <RichTextEditor.Toolbar sticky stickyOffset={0}>
        <RichTextEditor.ControlsGroup>
          <RichTextEditor.Bold />
          <RichTextEditor.Italic />
          <RichTextEditor.Strikethrough />
          <RichTextEditor.ClearFormatting />
        </RichTextEditor.ControlsGroup>

        <RichTextEditor.ControlsGroup>
          <RichTextEditor.H1 />
          <RichTextEditor.H2 />
          <RichTextEditor.H3 />
        </RichTextEditor.ControlsGroup>

        <RichTextEditor.ControlsGroup>
          <RichTextEditor.BulletList />
          <RichTextEditor.OrderedList />
        </RichTextEditor.ControlsGroup>

        <RichTextEditor.ControlsGroup>
          <RichTextEditor.Link />
          <RichTextEditor.Unlink />
        </RichTextEditor.ControlsGroup>
      </RichTextEditor.Toolbar>

      <RichTextEditor.Content />
    </RichTextEditor>
  );
};

export default RtfEditor;
