import axios from "axios"; 
import ApiRoutes from "../utils/ApiRoutes";

const createApi = (
    token: string | null,
    updateToken: (token: string) => void,
    logout: () => void
) => {

    const baseUrl = import.meta.env.VITE_APP_LOCAL_URL;

    const Api = axios.create({
        baseURL: baseUrl,
        headers: {
            "Content-Type": "application/json"
        },
        withCredentials: true
    })

    Api.interceptors.request.use(
        (config) => {
            if(token) {
                config.headers.Authorization = `Bearer ${token}`
            }
            return config
        },
        (error) => Promise.reject(error)
    )

    Api.interceptors.response.use(
        (response) => response,
        async (error) => {
            const initialRequest = error.config;

            if(error.response?.status === 401 && !initialRequest._retry){
                initialRequest._retry = true

                try{
                    const response = await axios.get(baseUrl + ApiRoutes.AUTH.refresh, {
                        withCredentials: true
                    })

                    const { token: newToken } = response!.data.result

                    updateToken(newToken)

                    initialRequest.headers.Authorization = `Bearer ${newToken}`
                    const RetryApi = createApi(newToken, updateToken, logout);

                    return RetryApi(initialRequest);
                }
                catch(error){
                    logout();
                }
            }
            return Promise.reject(error);
        }
    )
    return Api
}
export default createApi;