interface SpotifyEmbedProps {
  link: string;
}

const SpotifyEmbed = ({ link }: SpotifyEmbedProps) => {
  const getEmbedUrl = (url: string) => {
    return url.replace('open.spotify.com/', 'open.spotify.com/embed/');
  };

  return (
    <div style={{ borderRadius: '12px', overflow: 'hidden', display: 'block' }}>
      <iframe
        title="Spotify Embed"
        src={getEmbedUrl(link)}
        width="100%"
        height="80"
        allow="autoplay; clipboard-write; encrypted-media; fullscreen; picture-in-picture"
        loading="lazy"
        style={{
          border: 0,
          display: 'block',
        }}
      />
    </div>
  );
};

export default SpotifyEmbed;
