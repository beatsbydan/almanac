import React, { useState, useCallback, useEffect } from 'react'
import { Outlet, Navigate, useLocation } from 'react-router-dom'
import { useApi, useAuth } from '../hooks/useContexts'
import { alertFailure } from '../utils/Notifications'
import ApiRoutes from '../utils/ApiRoutes'
import Refresh from '../ui/Refresh'

const ReAuth:React.FC = () => {
    const { isLoggedIn, login, logout } = useAuth();
    const { makeGetRequest } = useApi();

    const location = useLocation();

    const [validating, setValidating] = useState<boolean>(true)

    const refresh = useCallback(async () => {
        if(!isLoggedIn){
            const { success, error, responseBody } = await makeGetRequest(ApiRoutes.AUTH.refresh);

            if(success){
                const { role, userEmail, userId, token, level, department } = responseBody!.result
                const payload = {
                    newRole: role,
                    newUser: {
                        id: userId,
                        email: userEmail,
                        level: level,
                        department: department
                    },
                    newToken: token 
                }
                login(payload)
            }
            else{
                logout()
                alertFailure(error!.result.message)
            }
        }
        setValidating(false);
    }, [isLoggedIn])

    useEffect(() => {
        refresh()
    },[refresh])

    return (
        validating ? <Refresh/> : (!validating && (isLoggedIn === true)) ? <Outlet/> : <Navigate to="/login" replace state={{from: location}}/>
    )
}

export default ReAuth