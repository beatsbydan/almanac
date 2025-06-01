import React, { useState, useEffect } from 'react'
import { useApi } from '../../../../hooks/useContexts';
import { GenericObjectType, ModalFormType } from '../../../../utils/Types';
import { alertFailure, alertSuccess } from '../../../../utils/Notifications';
import { ValidateDepartmentForm } from '../Validations';
import { X } from 'lucide-react'
import { motion } from 'framer-motion'
import { Input } from '../../../../ui/Input';
import Button from '../../../../ui/Button';
import Dropdown from '../../../../ui/Dropdown';
import ApiRoutes from '../../../../utils/ApiRoutes';

const Form:React.FC<ModalFormType & {closeModal: ()=>void, refetchMethod: () => void}> = ({closeModal, inputValues: inputFields, refetchMethod}) => {
    const { makePostRequest, makePutRequest } = useApi();

    useEffect(() => {
        if(inputFields){
            setInputValues({...inputFields})
        }
    }, [inputFields])

    const [inputValues, setInputValues] = useState<GenericObjectType>({
        departmentName: "",
        departmentShortName: "",
        college: ""
    })

    const [inputErrors, setInputErrors] = useState<GenericObjectType>({
        departmentName: {
            valid: false,
            message: ""
        },
        departmentShortName: {
            valid: false,
            message: ""
        },
        college: {
            valid: false,
            message: ""
        },
        none: false
    })

    const [dropdownValues, setDropDownValues] = useState<GenericObjectType>({
        college: []
    })

    const saveColleges = (e: GenericObjectType[]) => {
        const mappedColleges = e.map(college => college?.collegeShortName)
        setDropDownValues({...dropdownValues, colleges: [...mappedColleges]})
    }

    const [processing, setProcessing] = useState<boolean>(false);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const {name, value} = e.target;
        setInputValues(prev => {
            return {...prev, [name]: value}
        })
    }

    const clearInputFields = () => {
        setInputValues({
            departmentName: "",
            departmentShortName: "",
            college: ""
        })
        setInputErrors({
            departmentName: {
                valid: false,
                message: ""
            },
            departmentShortName: {
                valid: false,
                message: ""
            },
            college: {
                valid: false,
                message: ""
            },
            none: false
        })
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        const errors = ValidateDepartmentForm(inputValues);
        setInputErrors(errors);

        if(errors.none){
            setProcessing(true);
            const { success, error, responseBody } = inputFields ? await makePutRequest (
                ApiRoutes.DEPARTMENT.update + inputFields?.code,
                inputValues,

            ) : await makePostRequest(
                ApiRoutes.DEPARTMENT.create,
                inputValues
            )

            if(success){
                alertSuccess(responseBody!.result.message)
                clearInputFields()
                closeModal()
                refetchMethod()
            }
            if(error){
                alertFailure(error!.result.message)
            }
            setProcessing(false)
        }
        else{
            alertFailure("Some fields are still empty")
        }
    }

    const handleCollegeChange = (e: string) => {
        setInputValues({...inputValues, college: e})
    }

    return (
        <div className='w-full relative'>
            <X
                className="cursor-pointer right-0 absolute -top-8 text-light-gray"
                onClick={closeModal}
            />

            <form className='w-full flex flex-col gap-3 mt-7 items-end' onSubmit={handleSubmit}>
                <Input
                    name="departmentName"
                    type="text"
                    placeholder="Department"
                    label="Name"
                    error={inputErrors.departmentName}
                    value={inputValues.departmentName}
                    onChange={handleChange}
                    delay={0.2}
                />
                <Input
                    name="departmentShortName"
                    type="text"
                    placeholder="SHORT"
                    label="Short Name"
                    error={inputErrors.departmentShortName}
                    value={inputValues.departmentShortName}
                    onChange={handleChange}
                    delay={0.4}
                />
                <motion.div 
                    className="mt-3 w-full"
                    initial={{ opacity: 0, x: -50 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.3, ease: "easeOut", delay: 0.6 }}
                >
                    <Dropdown
                        save={handleCollegeChange}
                        placeHolder='College'
                        options={dropdownValues.colleges}
                        optionsApiRoute={ApiRoutes.COLLEGE.get}
                        value={inputValues.college}
                        saveOptions={saveColleges}
                        error={inputErrors.college}
                    />
                </motion.div>
                <Button
                    processing={processing}
                    type="submit"
                    styleClass='bg-bowen-blue hover:border-bowen-blue text-white font-semibold mt-3'
                    spinnerStyleClass='w-6 h-6 mx-auto'
                    delay={1.2}
                >
                    {inputFields ? "Update" : "Create"}
                </Button>
            </form>
        </div>
    )
}

export default React.memo(Form)