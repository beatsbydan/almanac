import React, { ReactNode } from 'react'
import { AuthContextProvider } from './AuthContext'
import { ApiContextProvider } from './ApiContext'
import { NavContextProvider } from './NavContext'
import { AppContextProvider } from './AppContext'

const Provider:React.FC<{children: ReactNode}> = ({children}) => {
    return (
        <AuthContextProvider>
            <ApiContextProvider>
                <NavContextProvider>
                    <AppContextProvider>
                        {children}
                    </AppContextProvider>
                </NavContextProvider>
            </ApiContextProvider>
        </AuthContextProvider>
    )
}

export default Provider