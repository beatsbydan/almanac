import React, { useState } from 'react'
import { GenericObjectType } from '../../../utils/Types'
import { ValidateResetPasswordForm } from './Validations'
import { alertFailure, alertSuccess } from '../../../utils/Notifications'
import { useLocation, useNavigate } from 'react-router-dom'
import { useApi } from '../../../hooks/useContexts'
import { Input } from '../../../ui/Input'
import Card from './Card'
import Button from '../../../ui/Button'
import ApiRoutes from '../../../utils/ApiRoutes'

const ResetPassword:React.FC = () => {
    const navigate = useNavigate()
    const location = useLocation()

    const { makePutRequest } = useApi()

    const [inputValues, setInputValues] = useState<GenericObjectType>({
        password: "",
        confirmPassword: ""
    })
    const [inputErrors, setInputErrors] = useState<GenericObjectType>({
        password: {
            valid: false,
            message: ""
        },
        confirmPassword: {
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
            password: "",
            confirmPassword: ""
        })
    }

    const handleSubmit = async () => {
        const errors = ValidateResetPasswordForm(inputValues);
        setInputErrors(errors);

        if(errors.none){
            setProcessing(true);
            const { success, error, responseBody } = await makePutRequest(
                ApiRoutes.AUTH.reset_password,
                {
                    newPassword: inputValues?.password
                }
            )

            if(success){
                alertSuccess(responseBody!.result.message)
                clearInputFields()
                navigate("/login", {replace:true, state:{from: location}})
            }
            else{
                alertFailure(error!.result.message)
            }
            setProcessing(false)
        }
    }

    return (
        <Card header='Reset Password' subHeader='Enter your new password!'>
            <div className='w-full h-full'>
                <form className='w-full flex flex-col gap-3 py-7 items-end'>
                    <Input
                        name="password"
                        type="password"
                        placeholder="* * * * * * * *"
                        label="New password"
                        error={inputErrors.password}
                        value={inputValues.password}
                        onChange={handleChange}
                        delay={0.2}
                    />
                    <Input
                        name="confirmPassword"
                        type="password"
                        placeholder="* * * * * * * *"
                        label="Confirm password"
                        error={inputErrors.confirmPassword}
                        value={inputValues.confirmPassword}
                        onChange={handleChange}
                        delay={0.4}
                    />
                </form>
                <Button
                    processing={processing}
                    onClick={handleSubmit}
                    styleClass='bg-bowen-blue hover:border-bowen-blue text-white font-semibold mt-7'
                    delay={0.6}
                    spinnerStyleClass='w-6 h-6 mx-auto'
                >
                    Reset Password
                </Button>
            </div>
        </Card>
    )
}

export default ResetPassword