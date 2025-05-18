import React, { useState } from 'react'
import { GenericObjectType } from '../../../utils/Types'
import { Link } from 'react-router-dom'
import { ValidateLoginForm } from './Validations'
import { useLocation, useNavigate } from 'react-router-dom'
import { alertFailure, alertSuccess } from '../../../utils/Notifications'
import { useApi, useAuth } from '../../../hooks/useContexts'
import { motion } from 'framer-motion'
import { Input } from '../../../ui/Input'
import ApiRoutes from '../../../utils/ApiRoutes'
import Card from './Card'
import Button from '../../../ui/Button'
import { ROLES } from '../../../utils/Constants'

const Login:React.FC = () => {
    const navigate = useNavigate()
    const location = useLocation()

    const { makePostRequest } = useApi();
    const { login } = useAuth();

    const [inputValues, setInputValues] = useState<GenericObjectType>({
        userId: "",
        password: ""
    })
    const [inputErrors, setInputErrors] = useState<GenericObjectType>({
        userId: {
            valid: false,
            message: ""
        },
        password: {
            valid: false,
            message: ""
        },
        none: false
    })

    const [processing, setProcessing] = useState<boolean>(false);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const {name, value} = e.target;
        setInputValues(prev => {
            return {...prev, [name]: value}
        })
    }

    const clearInputFields = () => {
        setInputValues({
            userId: "",
            password: ""
        })
        setInputErrors({
            userId: {
                valid: false,
                message: ""
            },
            password: {
                valid: false,
                message: ""
            },
            none: false
        })
    }

    const handleSubmit = async () => {
        const errors = ValidateLoginForm(inputValues);
        setInputErrors(errors);

        if(errors.none){
            setProcessing(true);
            const { success, error, responseBody } = await makePostRequest(
                ApiRoutes.AUTH.login,
                inputValues
            )

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
                login(payload);
                alertSuccess("Welcome Back!")
                clearInputFields()
                
                const entryPoint = role === ROLES.STUDENT ? "/timetables" : "/dashboard"

                navigate(entryPoint, {replace:true, state:{from: location}})
            }
            else{
                alertFailure(error!.result.message)
            }
            setProcessing(false)
        }
    }

    return (
        <Card header='Login' subHeader='Welcome back!'>
            <div className='w-full h-full'>
                <form className='w-full flex flex-col gap-3 py-7 items-end'>
                    <Input
                        name="userId"
                        type="text"
                        placeholder="BU-USER-ID"
                        label="User ID"
                        error={inputErrors.userId}
                        value={inputValues.userId}
                        onChange={handleChange}
                        delay={0.2}
                    />
                    <Input
                        name="password"
                        type="password"
                        placeholder="* * * * * * * *"
                        label="Password"
                        error={inputErrors.password}
                        value={inputValues.password}
                        onChange={handleChange}
                        delay={0.4}
                    />
                    <Link to="/verify-email" className='text-[14px] text-light-gray font-medium -mb-3'>Forgot password?</Link>
                </form>
                <motion.p 
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.6, ease: "easeOut", delay: 0.6 }}
                    className="text-light-gray text-center">Don't have an account? <span><Link to="/register" className="text-black font-semibold pb-1 border-b-orange border-b-[1.5px]">Register</Link></span></motion.p>
                <Button
                    processing={processing}
                    onClick={handleSubmit}
                    styleClass='bg-bowen-blue hover:border-bowen-blue text-white font-semibold mt-5'
                    delay={0.8}
                    spinnerStyleClass='w-6 h-6 mx-auto'
                >
                    Login
                </Button>
            </div>
        </Card>
    )
}

export default Login