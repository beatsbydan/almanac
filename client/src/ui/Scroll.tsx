import React, { ReactNode } from 'react';
import { OverlayScrollbarsComponent as ScrollContainer } from 'overlayscrollbars-react';
import 'overlayscrollbars/overlayscrollbars.css';

const Scroll:React.FC<{children: ReactNode}> = ({ children }) => {
    return (
        <ScrollContainer
            options={{
                scrollbars: {
                    autoHide: 'leave',
                },
            }}
            style={{ height: '100%', width:"100%" }}
        >
            {children}
        </ScrollContainer>
    );
};

export default React.memo(Scroll);