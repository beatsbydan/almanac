import React from 'react'
import { useLocation, Navigate, Outlet } from 'react-router-dom'
import { ROLES } from '../utils/Constants'
import { useAuth } from '../hooks/useContexts';

export const StaffPrivileges:React.FC = () => {
    const location = useLocation();
    const { role } = useAuth()

    const allowedRoles = [ROLES.ADMIN, ROLES.LECTURER]

    return (
        allowedRoles.includes(role!) ? <Outlet/> : <Navigate to="/login" replace state={{from: location}}/>
    )
}

export const AdminPrivileges:React.FC = () => {
    const location = useLocation();
    const { role } = useAuth()

    const allowedRoles = [ROLES.ADMIN]

    return (
        allowedRoles.includes(role!) ? <Outlet/> : <Navigate to="/login" replace state={{from: location}}/>
    )
}