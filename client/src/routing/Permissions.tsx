import React, { ReactNode, useMemo } from "react";
import { useAuth } from "../hooks/useContexts";
import { ROLES } from "../utils/Constants";

export const StudentPermission:React.FC<{children: ReactNode}> = ({children}) => {
    const { role } = useAuth();

    const isStudent = useMemo(() => {
        return role === ROLES.STUDENT
    },[role])

    return (
        <div className={isStudent ? "block" : "hidden"}>
            {children}
        </div>
    )
}

export const AdminPermission:React.FC<{children: ReactNode}> = ({children}) => {
    const { role } = useAuth();

    const isAdmin = useMemo(() => {
        return role === ROLES.ADMIN
    },[role])

    return (
        <div className={isAdmin ? "block" : "hidden"}>
            {children}
        </div>
    )
}

export const StaffPermission:React.FC<{children: ReactNode}> = ({children}) => {
    const { role } = useAuth();
    const allowedRoles = [ROLES.ADMIN, ROLES.LECTURER]

    const isPermitted = useMemo(() => {
        return allowedRoles.includes(role!)
    },[role])

    return (
        <div className={isPermitted ? "block" : "hidden"}>
            {children}
        </div>
    )
}

export const NotAdminPermission:React.FC<{children: ReactNode}> = ({children}) => {
    const { role } = useAuth();
    const allowedRoles = [ROLES.STUDENT, ROLES.LECTURER]

    const isPermitted = useMemo(() => {
        return allowedRoles.includes(role!)
    },[role])

    return (
        <div className={isPermitted ? "block" : "hidden"}>
            {children}
        </div>
    )
}

export const LecturerPermission:React.FC<{children: ReactNode}> = ({children}) => {
    const { role } = useAuth();
    const allowedRoles = [ROLES.LECTURER]

    const isPermitted = useMemo(() => {
        return allowedRoles.includes(role!)
    },[role])

    return (
        <div className={isPermitted ? "block" : "hidden"}>
            {children}
        </div>
    )
}