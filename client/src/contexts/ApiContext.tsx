import React, { ReactNode, useCallback, useMemo } from "react";
import { createContext } from "react";
import { ApiContextType, ApiResponseType, GenericObjectType } from "../utils/Types";
import { useAuth } from "../hooks/useContexts";
import createApi from "../configs/Api";
import { alertFailure } from "../utils/Notifications";

export const ApiContext = createContext<ApiContextType>({
    makeDownloadRequest: async () => ({
        success: false,
        error: {},
        responseBody: {},
    }),
    makeGetRequest: async () => ({
        success: false,
        error: {},
        responseBody: {},
    }),
    makePostRequest: async () => ({
        success: false,
        error: {},
        responseBody: {},
    }),
    makePutRequest: async () => ({
        success: false,
        error: {},
        responseBody: {},
    }),
    makeDeleteRequest: async () => ({
        success: false,
        error: {},
        responseBody: {},
    })
})

export const ApiContextProvider:React.FC<{children: ReactNode}> = ({children}) => {
    const { getToken, updateToken, logout } = useAuth();

    const exit = useCallback(() => {
        logout();
        alertFailure("Session Expired, Please log back in.");
    }, [])

    const Api = useMemo(() => {
        const token = getToken();
        return createApi(token, updateToken, exit)
    }, [getToken, updateToken, exit])

    
    const makePostRequest = async (
        url: string, 
        payload: GenericObjectType,
        params?: GenericObjectType
    ) => {
        let apiResponse:ApiResponseType = {
            success: false,
            error: null,
            responseBody: {}
        }
    
        try{
            const response = await Api.post(url, payload, { params })
        
            apiResponse.success = true;
            apiResponse.responseBody = response.data
        }
        catch(error: any){
            apiResponse.error = {...error.response.data}
        }
    
        return apiResponse;
    }
    
    const makeGetRequest = async (
        url: string, 
        params?:GenericObjectType
    ) => {
        let apiResponse:ApiResponseType = {
            success: false,
            error: null,
            responseBody: {}
        }
    
        try{
            const response = await Api.get(url, { params })    
            apiResponse.success = true;
            apiResponse.responseBody = response.data
        }
        catch(error: any){
            apiResponse.error = {...error.response.data}
        }
    
        return apiResponse;
    }
    
    const makePutRequest = async (
        url: string, 
        payload?: GenericObjectType,
        params?: GenericObjectType
    ) => {
        let apiResponse:ApiResponseType = {
            success: false,
            error: null,
            responseBody: {}
        }
    
        try{
            const response = await Api.put(url, payload, { params })
    
            apiResponse.success = true;
            apiResponse.responseBody = response.data
        }
        catch(error: any){
            apiResponse.error = {...error.response.data}
        }
    
        return apiResponse;
    }

    const makeDownloadRequest = async (url: string, params?:GenericObjectType) => {
        let apiResponse:any= {
            success: false,
            error: null,
            responseBody: null
        }

        try{
            const response = await Api.get(url,{
                params: {...params},
                responseType: 'blob'
            })
    
            apiResponse.success = true;
            apiResponse.responseBody = response.data
        }
        catch(error: any){
            apiResponse.error = {...error.response.data}
        }

        return apiResponse;
    }
    
    const makeDeleteRequest = async (
        url: string
    ) => {
        let apiResponse:ApiResponseType = {
            success: false,
            error: null,
            responseBody: {}
        }
    
        try{
            const response = await Api.delete(url)
    
            apiResponse.success = true;
            apiResponse.responseBody = response.data
        }
        catch(error: any){
            apiResponse.error = {...error.response.data}
        }
    
        return apiResponse;
    }

    const value:ApiContextType = {
        makeGetRequest,
        makePostRequest,
        makePutRequest, 
        makeDeleteRequest,
        makeDownloadRequest
    }
    return (
        <ApiContext.Provider value={value}>
            {children}
        </ApiContext.Provider>
    )
}