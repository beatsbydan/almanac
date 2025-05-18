import React, { useMemo, useState, useCallback } from 'react'
import { GenericObjectType } from '../../../../utils/Types'
import { Link } from 'react-router-dom'
import { ValidateRegistrationForm } from '../Validations'
import { useApi } from '../../../../hooks/useContexts'
import { alertFailure, alertSuccess } from '../../../../utils/Notifications'
import { useLocation, useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import { ComponentReveal as AuthCardComponentReveal } from '../../../../ui/animations/ComponentAnimations'
import Card from '../Card'
import Button from '../../../../ui/Button'
import ApiRoutes from '../../../../utils/ApiRoutes'
import StudentForm from './StudentForm'
import StaffForm from './StaffForm'

const Register:React.FC = () => {
    const navigate = useNavigate()
    const location = useLocation()

    const { makePostRequest } = useApi()
    const [currentForm, setCurrentForm] = useState<string>("student")

    const [inputValues, setInputValues] = useState<GenericObjectType>({
        title: "",
        firstName: "",
        lastName: "",
        phone: "",
        userEmail: "",
        userId: "",
        level: undefined,
        department: "",
        password: "",
        confirmPassword: ""
    })

    const isStudentForm = useMemo(() => {
        return currentForm === "student"
    },[currentForm])

    const pathVariable = useMemo(() => {
        return isStudentForm ? "/student" : "/staff"
    }, [isStudentForm])

    const [inputErrors, setInputErrors] = useState<GenericObjectType>({
        title:{
            valid: false,
            message: ""
        },
        firstName:{
            valid: false,
            message: ""
        },
        lastName:{
            valid: false,
            message: ""
        },
        phone:{
            valid: false,
            message: ""
        },
        userEmail: {
            valid: false,
            message: ""
        },
        userId: {
            valid: false,
            message: ""
        },
        level: {
            valid: false,
            message: ""
        },
        department: {
            valid: false,
            message: ""
        },
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

    const changeCurrentForm = useCallback((form: string) => {
        setCurrentForm(form);
        clearInputFields()
    }, [])

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const {name, value} = e.target;
        setInputValues(prev => {
            return {...prev, [name]: value}
        })
    }

    const clearInputFields = () => {
        setInputValues({
            title: "",
            firstName: "",
            lastName: "",
            phone: "",
            userEmail: "",
            userId: "",
            level: undefined,
            department: "",
            password: "",
            confirmPassword: ""
        })
        setInputErrors({
            title:{
                valid: false,
                message: ""
            },
            firstName:{
                valid: false,
                message: ""
            },
            lastName:{
                valid: false,
                message: ""
            },
            phone:{
                valid: false,
                message: ""
            },
            userEmail: {
                valid: false,
                message: ""
            },
            userId: {
                valid: false,
                message: ""
            },
            level: {
                valid: false,
                message: ""
            },
            department: {
                valid: false,
                message: ""
            },
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
    }

    const handleLevelChange = (e:string) => {
        setInputValues({...inputValues, level: e})
    }

    const handleDepartmentChange = (e:string) => {
        setInputValues({...inputValues, department: e})
    }

    const handleTitleChange = (e:string) => {
        setInputValues({...inputValues, title: e})
    }

    const handleSubmit = async () => {
        const errors = ValidateRegistrationForm(inputValues, isStudentForm)
        setInputErrors(errors);

        if(errors.none){
            const { userEmail, userId, level, ...rest } = inputValues;

            const studentInputFields = {
                studentEmail: userEmail,
                studentId: userId,
                level: level,
                ...rest,
            }

            const staffInputFields = {
                staffEmail: userEmail,
                staffId: userId,
                ...rest,
            }

            setProcessing(true);

            const { success, error, responseBody } = await makePostRequest(
                ApiRoutes.AUTH.register + pathVariable,
                isStudentForm ? {...studentInputFields} : {...staffInputFields}
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
        else{
            alertFailure(`Some fields are still empty..`)
        }
    }

    return (
        <Card header="Sign Up" subHeader='Create an account!'>
            <AuthCardComponentReveal 
                styleClass='w-full flex items-center z-10 justify-center gap-4 mt-2'
                delay={0.2}
                type='container'
            >
                <AuthCardComponentReveal
                    styleClass=''
                    type='container'
                    delay={0.4}
                >
                    <button 
                        type="button" 
                        className={`${isStudentForm ? "bg-greyish-beige text-white shadow-lg" : ""} cursor-pointer py-3 rounded-lg border-1 border-transparent hover:border-greyish-beige duration-500 transition-colors px-5`} 
                        onClick={()=>changeCurrentForm("student")}
                    >
                        Student
                    </button>
                </AuthCardComponentReveal>
                <AuthCardComponentReveal
                    styleClass=''
                    type='container'
                    delay={0.6}
                >
                    <button 
                        type="button" 
                        className={`${!isStudentForm ? "bg-greyish-beige text-white shadow-lg" : ""} cursor-pointer py-3 rounded-lg border-1 border-transparent hover:border-greyish-beige duration-500 transition-colors px-5`} 
                        onClick={()=>changeCurrentForm("staff")}
                    >
                            Staff
                        </button>
                </AuthCardComponentReveal>
            </AuthCardComponentReveal>
            <div className='w-full h-[calc(83vh-160px)]'>
                {isStudentForm ? 
                    <StudentForm
                        inputValues={inputValues}
                        inputErrors={inputErrors}
                        handleLevelChange={handleLevelChange}
                        handleDepartmentChange={handleDepartmentChange}
                        handleChange={handleChange}
                    /> 
                    : 
                    <StaffForm
                        inputValues={inputValues}
                        inputErrors={inputErrors}
                        handleChange={handleChange}
                        handleTitleChange={handleTitleChange}
                    />
                }
                <motion.p 
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ duration: 0.6, ease: "easeOut", delay: 2.0 }}
                    className="text-light-gray text-center">Already have an account? <span><Link to="/login" className="text-black font-semibold pb-1 border-b-orange border-b-[1.5px]">Login</Link></span></motion.p>
                <Button
                    processing={processing}
                    onClick={handleSubmit}
                    styleClass='bg-bowen-blue hover:border-bowen-blue text-white font-semibold mt-5'
                    spinnerStyleClass='w-6 h-6 mx-auto'
                    delay={2.2}
                >
                    Create Account
                </Button>
            </div>
        </Card>
    )
}

export default Register