import React, { useState } from 'react'
import { GenericObjectType } from '../../../utils/Types'
import { ValidateEmailVerificationForm } from './Validations'
import { useLocation, useNavigate } from 'react-router-dom'
import { useApi } from '../../../hooks/useContexts'
import { alertFailure, alertSuccess } from '../../../utils/Notifications'
import { Input } from '../../../ui/Input'
import Card from './Card'
import Button from '../../../ui/Button'
import ApiRoutes from '../../../utils/ApiRoutes'

const VerifyEmail:React.FC = () => {
    const location = useLocation()
    const navigate = useNavigate()

    const { makePostRequest } = useApi()

    const [inputValues, setInputValues] = useState<GenericObjectType>({
        userEmail: ""
    })
    const [inputErrors, setInputErrors] = useState<GenericObjectType>({
        userEmail: {
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
            userEmail: ""
        })
        setInputErrors({
            userEmail: {
                valid: false,
                message: ""
            },
            none: false
        })
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()

        const errors = ValidateEmailVerificationForm(inputValues);
        setInputErrors(errors);

        if(errors.none){
            setProcessing(true);
            const { success, error, responseBody } = await makePostRequest(
                ApiRoutes.AUTH.verify_email,
                inputValues
            )

            if(success){
                alertSuccess(responseBody!.result.message)
                clearInputFields()
                navigate("/verify-otp", {replace:true, state:{from: location}})
            }
            else{
                alertFailure(error!.result.message)
            }
            setProcessing(false)
        }
    }

    return (
        <Card header='Verify Email' subHeader='Enter your email to start the recovery process!'>
            <div className='w-full h-full'>
                <form className='w-full pt-7' onSubmit={handleSubmit}>
                    <Input
                        name="staffEmail"
                        type="text"
                        placeholder="example@bowen.edu.ng"
                        label="Email"
                        error={inputErrors.userEmail}
                        value={inputValues.userEmail}
                        onChange={handleChange}
                        delay={0.2}
                    />
                    <Button
                        processing={processing}
                        type='submit'
                        styleClass='bg-bowen-blue hover:border-bowen-blue text-white mt-5 font-semibold'
                        delay={0.4}
                        spinnerStyleClass='w-6 h-6 mx-auto'
                    >
                        Send
                    </Button>
                </form>
            </div>
        </Card>
    )
}

export default VerifyEmail