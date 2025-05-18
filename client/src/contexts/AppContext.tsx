import React, { ReactNode, createContext } from 'react'
import { AppContextType } from '../utils/Types'

const AppContext = createContext<AppContextType>({

})

export const AppContextProvider:React.FC<{children: ReactNode}> = ({children}) => {
    const value = {

    }

    return (
        <AppContext.Provider value={value}>
            {children}
        </AppContext.Provider>
    )
}