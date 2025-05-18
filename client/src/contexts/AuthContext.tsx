import React, { ReactNode, createContext, useState, useCallback } from 'react'
import { AuthContextType, GenericObjectType } from '../utils/Types'

export const AuthContext = createContext<AuthContextType>({
    user: null,
    token: null,
    role: null,
    isLoggedIn: false,
    getToken: () => "",
    login: () => {},
    updateToken: () => {},
    logout: () => {}
})

export const AuthContextProvider:React.FC<{children: ReactNode}> = ({children}) => {
    const [user, setUser] = useState<GenericObjectType | null>(null);
    const [token, setToken] = useState<string | null>(null);
    const [role, setRole] = useState<string | null>(null);
    const [isLoggedIn, setLoggedIn] = useState<boolean | string>(false);

    const login = (payload: GenericObjectType) => {
        const { newUser, newToken, newRole} = payload;

        setUser({...newUser})
        setToken(newToken)
        setRole(newRole)
        setLoggedIn(true)
    }

    const updateToken = useCallback(async (token: string) => {
        setToken(token)
    }, [])

    const getToken = useCallback(() => token, [token]);

    const logout = () => {
        setUser(null)
        setToken(null)
        setRole(null)
        setLoggedIn("USER_HAS_BEEN_LOGGED_OUT")
    }

    const value:AuthContextType = {
        user,
        token,
        role,
        isLoggedIn,
        login,
        getToken,
        updateToken,
        logout
    }   

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    )
}