import { ReactNode, createContext, useState, useCallback } from "react";
import { NavContextType } from "../utils/Types";

export const NavContext = createContext<NavContextType>({
    currentPage: "",
    changeCurrentPage: () => {}
})

export const NavContextProvider:React.FC<{children: ReactNode}> = ({children}) => {
    const [currentPage, setCurrentPage] = useState<string>("")
    const changeCurrentPage = useCallback((page: string) => {
        setCurrentPage(page);
    }, [])
    
    const value = {
        currentPage,
        changeCurrentPage,
    }
    return (
        <NavContext.Provider value={value}>
            {children}
        </NavContext.Provider>
    )
}