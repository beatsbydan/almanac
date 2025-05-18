import React, { useState, useEffect } from 'react'
import { useApi } from '../../../../hooks/useContexts';
import { GenericObjectType, ModalFormType } from '../../../../utils/Types';
import { alertFailure, alertSuccess } from '../../../../utils/Notifications';
import { ValidateClassroomForm } from '../Validations';
import { X } from 'lucide-react'
import { motion } from 'framer-motion'
import { Input } from '../../../../ui/Input';
import Button from '../../../../ui/Button';
import Dropdown from '../../../../ui/Dropdown';
import ApiRoutes from '../../../../utils/ApiRoutes';

const Form:React.FC<ModalFormType & {closeModal: ()=>void, refetchMethod: ()=>void}> = ({closeModal, inputValues: inputFields, refetchMethod}) => {
    const { makePostRequest, makePutRequest } = useApi();

    useEffect(() => {
        if(inputFields){
            setInputValues({...inputFields})
        }
    }, [inputFields])

    const [inputValues, setInputValues] = useState<GenericObjectType>({
        className: "",
        classSize: "",
        college: undefined
    })

    const [inputErrors, setInputErrors] = useState<GenericObjectType>({
        className: {
            valid: false,
            message: ""
        },
        classSize: {
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
            className: "",
            classSize: "",
            college: ""
        })
        setInputErrors({
            className: {
                valid: false,
                message: ""
            },
            classSize: {
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

    const handleSubmit = async () => {
        const errors = ValidateClassroomForm(inputValues);
        setInputErrors(errors);

        if(errors.none){
            setProcessing(true);
            const { success, error, responseBody } = inputFields ? await makePutRequest (
                ApiRoutes.CLASS_ROOM.update + inputFields?.code,
                inputValues,

            ) : await makePostRequest(
                ApiRoutes.CLASS_ROOM.create,
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
        <div className='w-full h-[calc(83vh-180px)] relative'>
            <X
                className="cursor-pointer right-0 absolute -top-8 text-light-gray"
                onClick={closeModal}
            />

            <form className='w-full flex flex-col gap-3 mt-7 mb-5 items-end'>
                <Input
                    name="className"
                    type="text"
                    placeholder="COCCS LR1"
                    label="Class Name"
                    error={inputErrors.className}
                    value={inputValues.className}
                    onChange={handleChange}
                    delay={0.2}
                />
                <Input
                    name="classSize"
                    type="text"
                    placeholder="500"
                    label="Class Size"
                    error={inputErrors.classSize}
                    value={inputValues.classSize}
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
                        value={inputValues.college}
                        optionsApiRoute={ApiRoutes.COLLEGE.get}
                        saveOptions={saveColleges}
                        error={inputErrors.college}
                    />
                </motion.div>
            </form>
            <Button
                processing={processing}
                onClick={handleSubmit}
                styleClass='bg-bowen-blue hover:border-bowen-blue text-white font-semibold mt-5'
                spinnerStyleClass='w-6 h-6 mx-auto'
                delay={1.2}
            >
                {inputFields ? "Update" : "Create"}
            </Button>
        </div>
    )
}

export default React.memo(Form)