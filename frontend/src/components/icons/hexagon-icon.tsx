

const HexagonIcon = ({ color, children, size = 100 }: any) => {
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
                style={{ position: 'absolute', top: 0, left: 0 }}
            >
                <defs>
                    <filter id="shadow" x="-20%" y="-20%" width="140%" height="140%">
                        <feDropShadow dx="0" dy="4" stdDeviation="4" floodColor="#000" floodOpacity="0.2" />
                    </filter>
                </defs>
                <polygon
                    points="50,5 95,31 95,84 50,110 5,84 5,31"
                    fill={color}
                    filter="url(#shadow)"
                />
            </svg>
            <div style={{ position: 'relative', zIndex: 1, color: 'white', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>{children}</div>
        </div>
    );
};

export default HexagonIcon;