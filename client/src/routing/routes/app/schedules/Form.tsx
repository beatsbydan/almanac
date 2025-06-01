import React, { useState, useEffect, useCallback } from 'react'
import { useApi, useAuth } from '../../../../hooks/useContexts';
import { GenericObjectType, ModalFormType } from '../../../../utils/Types';
import { alertFailure, alertSuccess } from '../../../../utils/Notifications';
import { ValidateScheduleForm } from '../Validations';
import { X } from 'lucide-react'
import { motion } from 'framer-motion'
import { ListInput } from '../../../../ui/Input';
import { TimePeriods, DaysOfTheWeek } from '../../../../utils/Constants';
import Button from '../../../../ui/Button';
import Dropdown from '../../../../ui/Dropdown';
import ApiRoutes from '../../../../utils/ApiRoutes';
import Scroll from '../../../../ui/Scroll';

const Form:React.FC<ModalFormType & {closeModal: ()=>void, suggestion: GenericObjectType | undefined, refetchMethod: ()=> void}> = ({closeModal, inputValues: inputFields, suggestion, refetchMethod}) => {
    const { makePostRequest, makePutRequest } = useApi();
    const { user } = useAuth()

    useEffect(() => {
        if(inputFields) setInputValues({...inputFields})
    }, [inputFields])

    useEffect(() => {
        if(suggestion){
            saveClassrooms(suggestion?.classrooms)
            setInputValues(suggestion)
        }
    }, [suggestion])

    const [inputValues, setInputValues] = useState<GenericObjectType>({
        recurringDay: "",
        startTime: "",
        timeDuration: "",
        course: {},
        classrooms: []
    })
    const [inputErrors, setInputErrors] = useState<GenericObjectType>({
        recurringDay: {
            valid: false,
            message: ""
        },
        startTime: {
            valid: false,
            message: ""
        },
        timeDuration: {
            valid: false,
            message: ""
        },
        course: {
            valid: false,
            message: ""
        },
        classrooms:{
            valid: false,
            message: ""
        },
        none: false
    })
    const [scheduleParamStore, setScheduleParamStore] = useState<GenericObjectType>({
        classrooms: [],
        courses: []
    })
    const [dropdownValues, setDropDownValues] = useState<GenericObjectType>({
        classrooms: [],
        courses: []
    })

    const [processing, setProcessing] = useState<boolean>(false);

    const saveClassrooms = (e: GenericObjectType[]) => {
        setScheduleParamStore({...scheduleParamStore, classrooms: [...e]})

        const mappedClassrooms = e.map(classroom => {
            return `${classroom?.college}, ${classroom?.className}`
        })

        setDropDownValues({...dropdownValues, classrooms: [...mappedClassrooms]})
    }
    
    const saveCourses = (e: GenericObjectType[]) => {
        setScheduleParamStore({...scheduleParamStore, courses: [...e]})

        const mappedCourses = e.map(course => {
            const courseCodeString = course?.courseCodes?.join(", ")
            return courseCodeString
        })
        setDropDownValues({...dropdownValues, courses: [...mappedCourses]})
    }
    
    const handleStartTimeChange = ((e: string) => {
        setInputValues({...inputValues, startTime: e})
    })
    const handleTimeDurationChange = ((e: string) => {
        setInputValues({...inputValues, timeDuration: e})
    })
    const handleDayChange = ((e: string) => {
        setInputValues({...inputValues, recurringDay: e})
    })

    const handleCourseChange = useCallback((e:string) => {
        const courseCodes = e.split(", ");
        const course = scheduleParamStore?.courses?.find((course: GenericObjectType) => course?.courseCodes?.includes(courseCodes[0]))
        setInputValues({...inputValues, course: course})
    }, [scheduleParamStore])

    const handleClassRoomsChange = useCallback((values: string[]) => {
        const classrooms: GenericObjectType[] = []
        for(let value of values){
            const className = value.split(", ")[1]
            const classroom = scheduleParamStore?.classrooms?.find((room: GenericObjectType) => ((room?.className === className) || (room?.className === value)))
            classrooms.push(classroom)
        }
        setInputValues(prev => {
            return {...prev, classrooms: [...classrooms]}
        })
    }, [scheduleParamStore])

    const clearInputFields = () => {
        setInputValues({
            recurringDay: "",
            startTime: "",
            timeDuration: "",
            course: {},
            classrooms: []
        })
        setInputErrors({
            recurringDay: {
                valid: false,
                message: ""
            },
            timeDuration: {
                valid: false,
                message: ""
            },
            startTime: {
                valid: false,
                message: ""
            },
            course: {
                valid: false,
                message: ""
            },
            classrooms:{
                valid: false,
                message: ""
            },
            none: false
        })
    }

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault()
        const payload = {
            userId: user?.id,
            recurringDay: inputValues.recurringDay,
            startTime: inputValues.startTime,
            timeDuration: inputValues.timeDuration,
            courseCode: inputValues.course.code,
            classroomCodes: inputValues?.classrooms?.map((room: GenericObjectType) => room?.code)
        }

        const errors = ValidateScheduleForm(payload);
        setInputErrors(errors)
        if(errors.none){
            setProcessing(true);
            const { success, error, responseBody } = inputFields ? await makePutRequest (
                ApiRoutes.SCHEDULE.update + inputFields?.code,
                {...payload},

            ) : await makePostRequest(
                ApiRoutes.SCHEDULE.create,
                {...payload}
            )

            if(success){
                alertSuccess(responseBody!.result.message)
                clearInputFields()
                closeModal()
                refetchMethod()
            }
            if(error){
                alertFailure(error!.result.message)
                setProcessing(false)
            }
            setProcessing(false)
        }
        else{
            alertFailure("Some fields are still empty");
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
            <div className='w-full'>
                <Scroll>
                    <form className='w-full flex flex-col gap-1 items-end h-full' onSubmit={handleSubmit}>
                        <motion.div 
                            className="mt-3 w-full"
                            initial={{ opacity: 0, x: -50 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ duration: 0.3, ease: "easeOut", delay: 0.2 }}
                        >
                            <Dropdown
                                save={handleDayChange}
                                placeHolder='Day'
                                options={DaysOfTheWeek}
                                value={inputValues.recurringDay}
                                error={inputErrors.recurringDay}
                            />
                        </motion.div>
                        <motion.div 
                            className="mt-3 w-full"
                            initial={{ opacity: 0, x: -50 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ duration: 0.3, ease: "easeOut", delay: 0.4 }}
                        >
                            <Dropdown
                                save={handleStartTimeChange}
                                placeHolder='Select a start time..'
                                options={TimePeriods}
                                value={inputValues.startTime}
                                error={inputErrors.startTime}
                            />
                        </motion.div>
                        <motion.div 
                            className="mt-3 w-full"
                            initial={{ opacity: 0, x: -50 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ duration: 0.3, ease: "easeOut", delay: 0.4 }}
                        >
                            <Dropdown
                                save={handleTimeDurationChange}
                                placeHolder='Time Period'
                                options={["1 hour", "2 hours"]}
                                value={inputValues.timeDuration}
                                error={inputErrors.timeDuration}
                            />
                        </motion.div>
                        {!inputFields && <motion.div 
                            className="mt-3 w-full"
                            initial={{ opacity: 0, x: -50 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ duration: 0.3, ease: "easeOut", delay: 0.6 }}
                        >
                            <Dropdown
                                save={handleCourseChange}
                                saveOptions={saveCourses}
                                placeHolder='Course'
                                options={dropdownValues.courses}
                                optionsApiRoute={ApiRoutes.COURSE.filter}
                                value={inputValues.course?.courseTitle}
                                canSearch={true}
                                searchOnly={true}
                                error={inputErrors.course}
                                searchPlaceholder="Search course by course code ot title.."
                            />
                        </motion.div>}
                        <motion.div 
                            className="mt-3 w-full"
                            initial={{ opacity: 0, x: -50 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ duration: 0.3, ease: "easeOut", delay: 0.8 }}
                        >
                            <ListInput
                                save={handleClassRoomsChange}
                                saveOptions={saveClassrooms}
                                inputValuesList={inputValues?.classrooms?.map((room: GenericObjectType) => room?.className)}
                                options={dropdownValues.classrooms}
                                placeHolder='Classroom(s)'
                                searchPlaceHolder='Search classroom by name or college...'
                                emptyText='classroom(s)'
                                isDropdown={true}
                                canSearch={true}
                                isSearch={true}
                                error={inputErrors.classrooms}
                                optionsApiRoute={ApiRoutes.CLASS_ROOM.filter}
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
                    
                </Scroll>
            </div>
        </div>
    )
}

export default React.memo(Form)