import React, { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { alertFailure, alertSuccess } from '../../../utils/Notifications';
import { useApi } from '../../../hooks/useContexts'
import { motion } from 'framer-motion'
import OtpInput from 'react-otp-input';
import Card from './Card';
import ApiRoutes from '../../../utils/ApiRoutes';
import Spinner from '../../../ui/Spinner';

const VerifyOtp:React.FC = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { makePostRequest } = useApi()

    const [otp, setOtp] = useState<string>('')
    const [focusedInput, setFocusedInput] = useState<any>(null);
    const [disabled, setDisabled] = useState<boolean>(true);
    const [processing, setProcessing] = useState<boolean>(false);

    useEffect(() => {
        if(otp?.length === 4) setDisabled(false)
        else setDisabled(true);
    },[otp])

    const clearInputFields = () => {
        setOtp("")
    }

    const handleSubmit = async() => {
        setProcessing(true);
        const { success, error, responseBody } = await makePostRequest(
            ApiRoutes.AUTH.verify_otp,
            {otp: otp}
        )

        if(success){
            alertSuccess(responseBody!.result.message)
            clearInputFields()
            navigate("/reset-password", {replace:true, state:{from: location}})
        }
        else{
            alertFailure(error!.result.message)
        }
        setProcessing(false)
    }

    return (
        <Card header='Verify OTP' subHeader='Enter the OTP sent to your mail!'>
            <div className="w-full h-full">
                <form className=" w-full flex flex-col gap-4" onSubmit={handleSubmit}>
                    <div className="w-full max-w-[300px] mx-auto mt-12">
                        <OtpInput
                            value={otp}
                            onChange={setOtp}
                            numInputs={4}
                            containerStyle={{
                                padding: '0',
                                gap: '1rem',
                                marginBottom: '1.5rem',
                            }}
                            inputStyle={{
                                width: '50px',
                                height: '50px',
                                padding: '1rem 0.7rem',
                                border: '1px solid rgba(128, 128, 128, 0.65)',
                                textAlign: 'center',
                                fontSize: "20px",
                                borderRadius: "10px",
                                margin: "auto"
                            }}
                            renderInput={(props, index) => (
                                <input
                                    {...props}
                                    style={{
                                        ...props.style,
                                        borderColor: focusedInput === index ? '#126da8' : 'rgba(128, 128, 128, 0.65)', 
                                    }}
                                    onFocus={() => setFocusedInput(index)}
                                    onBlur={() => setFocusedInput(null)}
                                />
                            )}
                            skipDefaultStyles
                        />
                    </div>
                    <motion.p 
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.6, ease: "easeOut", delay: 0.2 }}
                        className="text-light-gray text-center">Didn't receive the email? <span><Link to="/verify-email" className="text-black font-semibold pb-1 border-b-orange border-b-[1.5px]">Click here</Link></span></motion.p>
                    <motion.button
                        type='button'
                        className={"rounded-lg p-3 w-full cursor-pointer transition-colors ease-in-out border-1  text-white font-semibold mt-5  " + ` ${disabled? "bg-light-gray border-transparent" : processing ? "bg-transparent border-bowen-blue" : "hover:bg-transparent hover:text-black bg-bowen-blue border-transparent hover:border-bowen-blue"}`}
                        onClick={handleSubmit}
                        disabled={disabled || processing}
                        initial={{ opacity: 0, y: 20 }}
                        animate={{ opacity: 1, y: 0 }}
                        transition={{ duration: 0.6, ease: "easeOut", delay: 0.4 }}
                    >
                        {processing? <Spinner spinnerStyleClass='w-6 h-6 mx-auto'/> : "Verify"}
                    </motion.button>
                </form>
            </div>
        </Card>
    )
}

export default VerifyOtp;