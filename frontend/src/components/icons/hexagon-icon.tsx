interface HexagonIconProps {
  color: string;
  children: React.ReactNode;
  size?: number;
  selected?: boolean;
}

const HexagonIcon = ({ color, children, size = 100, selected }: HexagonIconProps) => {
  return (
    <div
      style={{
        position: 'relative',
        width: size,
        height: size * 1.15,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <svg
        width={size}
        height={size * 1.15}
        viewBox="0 0 100 115"
        style={{ position: 'absolute', top: 0, left: 0, overflow: 'visible' }}
      >
        <defs>
          <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
            <feDropShadow dx="0" dy="6" stdDeviation="6" floodColor="#0f172a" floodOpacity="0.12" />
          </filter>
        </defs>
        <polygon points="50,5 95,31 95,84 50,110 5,84 5,31" fill={color} filter="url(#shadow)" />
        <polygon
          points="50,10 90,33 90,82 50,105 10,82 10,33"
          fill="none"
          stroke="rgba(255,255,255,0.7)"
          strokeWidth="1.5"
        />
        {selected && (
          <polygon
            points="50,5 95,31 95,84 50,110 5,84 5,31"
            fill="none"
            stroke="rgba(31, 41, 55, 0.18)"
            strokeWidth="2.5"
            style={{
              transformOrigin: '50px 57.5px',
              transform: 'scale(1.08)',
              transition: 'transform 0.2s ease',
            }}
          />
        )}
      </svg>
      <div
        style={{
          position: 'relative',
          zIndex: 1,
          color: 'white',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
        }}
      >
        {children}
      </div>
    </div>
  );
};

export default HexagonIcon;
