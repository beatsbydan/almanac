import React, { useState, useEffect } from 'react'
import { useApi } from '../../../../../hooks/useContexts';
import { GenericObjectType, ModalFormType } from '../../../../../utils/Types';
import { alertFailure, alertSuccess } from '../../../../../utils/Notifications';
import { ValidateCourseForm } from '../../Validations';
import { X } from 'lucide-react'
import { motion } from 'framer-motion'
import { Input, ListInput, RadioButtonInput } from '../../../../../ui/Input';
import Button from '../../../../../ui/Button';
import Dropdown from '../../../../../ui/Dropdown';
import ApiRoutes from '../../../../../utils/ApiRoutes';
import Scroll from '../../../../../ui/Scroll';
import CourseParticipants from './CourseParticipant/CourseParticipants';
import { ClassSizes } from '../../../../../utils/Constants';

const Form:React.FC<ModalFormType & {closeModal: ()=>void, refetchMethod: () => void}> = ({closeModal, inputValues: inputFields, refetchMethod}) => {
    const { makePostRequest, makePutRequest } = useApi();

    useEffect(() => {
        if(inputFields){
            setInputValues({...inputFields})
        }
    }, [inputFields])

    const [inputValues, setInputValues] = useState<GenericObjectType>({
        courseTitle: "",
        courseUnit: "",
        courseCodes: [],
        courseParticipants: [],
        expectedClassSize: undefined,
        shared: false,
        batched: false,
        general: false
    })

    const [inputErrors, setInputErrors] = useState<GenericObjectType>({
        courseTitle: {
            valid: false,
            message: ""
        },
        courseUnit: {
            valid: false,
            message: ""
        },
        courseCodes: {
            valid: false,
            message: ""
        },
        courseParticipants: {
            valid: false,
            message: ""
        },
        expectedClassSize: {
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

    const handleCourseCodes =(values: string[]) => {
        setInputValues({...inputValues, courseCodes: [...values]})
    }
    const handleClassSizeChange = (e: string) => {
        setInputValues({...inputValues, expectedClassSize: e})
    }
    const handleSharedChange = () => {
        setInputValues({...inputValues, shared: !inputValues.shared})
    }
    const handleBatchedChange = () => {
        setInputValues({...inputValues, batched: !inputValues.batched})
    }
    const handleGeneralChange = () => {
        setInputValues({...inputValues, general: !inputValues.general})
    }

    const saveParticipant = (e: GenericObjectType) => {
        setInputValues((prev) => {
            return {...prev, courseParticipants: [...prev?.courseParticipants, {...e}]}
        })
    }

    const updateParticipant = (e: GenericObjectType) => {
        setInputValues((prev) => {
            let participant = prev?.courseParticipants?.find((participant:GenericObjectType) => participant?.level === e.level)
            participant = {...e}

            return {...prev, courseParticipants: [...prev?.courseParticipants?.filter((participant:GenericObjectType) => participant?.level !== e.level), {...participant}]}
        })
    }
    
    const removeParticipant = (e: GenericObjectType) => {
        setInputValues((prev) => {
            return {...prev, courseParticipants: [...prev?.courseParticipants?.filter((participant:GenericObjectType) => participant?.level !== e.level)]}
        })
    }

    const clearInputFields = () => {
        setInputValues({
            courseTitle: "",
            courseUnit: "",
            courseCodes: [],
            expectedClassSize: undefined,
            courseParticipants: [],
            isShared: false,
            isBatched: false,
            isGeneral: false
        })
        setInputErrors({
            courseTitle: {
                valid: false,
                message: ""
            },
            courseUnit: {
                valid: false,
                message: ""
            },
            courseCodes: {
                valid: false,
                message: ""
            },
            courseParticipants: {
                valid: false,
                message: ""
            },
            expectedClassSize: {
                valid: false,
                message: ""
            },
            none: false
        })
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        const errors = ValidateCourseForm(inputValues);
        setInputErrors(errors);

        if(errors.none){
            setProcessing(true);
            const { success, error, responseBody } = inputFields ? await makePutRequest (
                ApiRoutes.COURSE.update + inputFields?.code,
                inputValues,

            ) : await makePostRequest(
                ApiRoutes.COURSE.create,
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

    return (
        <div className='w-full relative'>
            <div className='w-full flex justify-end'>
                <X
                    className="cursor-pointer right-0 flex justify-end text-light-gray"
                    onClick={closeModal}
                />
            </div>
            <div className='w-full h-full'>
                <Scroll>
                    <form className='w-full flex flex-col gap-3 items-end h-full' onSubmit={handleSubmit}>
                        <Input
                            name="courseTitle"
                            type="text"
                            placeholder="Introduction to Bowen University"
                            label="Course Title"
                            error={inputErrors.courseTitle}
                            value={inputValues.courseTitle}
                            onChange={handleChange}
                            delay={0.8}
                        />
                        <Input
                            name="courseUnit"
                            type="text"
                            placeholder="0"
                            label="Course Unit"
                            error={inputErrors.courseUnit}
                            value={inputValues.courseUnit}
                            onChange={handleChange}
                            delay={1.0}
                        />
                        <motion.div 
                            className="w-full"
                            initial={{ opacity: 0, x: -50 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ duration: 0.3, ease: "easeOut", delay: 0.2 }}
                        >
                            <ListInput
                                save={handleCourseCodes}
                                searchPlaceHolder='Enter course code..'
                                inputValuesList={inputValues?.courseCodes}
                                isDropdown={false}
                                emptyText='course codes'
                                error={inputErrors.courseCodes}
                            />
                        </motion.div>
                        <CourseParticipants 
                            save={saveParticipant} 
                            updateParticipant={updateParticipant}
                            removeParticipant={removeParticipant} 
                            participants={inputValues?.courseParticipants}
                        />
                        <motion.div 
                            className="w-full"
                            initial={{ opacity: 0, x: -50 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ duration: 0.3, ease: "easeOut", delay: 0.6 }}
                        >
                            <Dropdown
                                save={handleClassSizeChange}
                                placeHolder='Expected Class Size'
                                options={ClassSizes}
                                value={inputValues.expectedClassSize}
                                error={inputErrors.expectedClassSize}
                            />
                        </motion.div>
                        <div className='w-full flex items-center justify-center gap-5 my-3'>
                            <RadioButtonInput
                                handleChange={handleBatchedChange}
                                value={inputValues.batched}
                                label={"Batched course"}
                                delay={1.2}
                            />
                            <RadioButtonInput
                                handleChange={handleSharedChange}
                                value={inputValues.shared}
                                label={"Shared course"}
                                delay={1.4}
                            />
                            <RadioButtonInput
                                handleChange={handleGeneralChange}
                                value={inputValues.general}
                                label={"General course"}
                                delay={1.6}
                            />
                        </div>
                        <Button
                            processing={processing}
                            type="submit"
                            styleClass='bg-bowen-blue hover:border-bowen-blue text-white font-semibold'
                            spinnerStyleClass='w-6 h-6 mx-auto'
                            delay={1.2}
                        >
                            {inputFields ? "Update" : "Create"}
                        </Button>
                    </form>
                </Scroll>
            </div>
        </div>
    )
}

export default React.memo(Form)