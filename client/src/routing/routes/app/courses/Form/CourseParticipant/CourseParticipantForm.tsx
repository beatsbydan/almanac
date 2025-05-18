import React,{ useEffect, useState } from 'react'
import { CourseParticipantFormType, GenericObjectType } from '../../../../../../utils/Types'
import { motion } from 'framer-motion'
import { ListInput } from '../../../../../../ui/Input'
import { X } from 'lucide-react'
import { ValidateCourseParticipantForm } from '../../../Validations'
import Scroll from '../../../../../../ui/Scroll'
import Dropdown from '../../../../../../ui/Dropdown'
import Button from '../../../../../../ui/Button'
import ApiRoutes from '../../../../../../utils/ApiRoutes'

const CourseParticipantForm:React.FC<CourseParticipantFormType> = ({save, update, closeModal, participant }) => {
    const [inputValues, setInputValues] = useState<GenericObjectType>({
        level: "",
        departments: []
    })
    const [inputErrors, setInputErrors] = useState<GenericObjectType>({
        level: {
            valid: false,
            message: ""
        },
        departments: {
            valid: false,
            message: ""
        },
        none: false
    })
    const [dropDownOptions, setDropDownOptions] = useState<GenericObjectType>({
        departments: [],
        colleges: []
    })

    useEffect(() => {
        if(participant){
            setInputValues({...participant})
        }
    }, [participant])

    const saveDepartments = (fetchedDepartments: GenericObjectType[]) => {
        const filteredDepartments = fetchedDepartments.map(department => department?.departmentName)
        setDropDownOptions({...dropDownOptions, departments: [...filteredDepartments]})
    }

    const handleLevels =(value: string) => {
        setInputValues({...inputValues, level: value})
    }
    const handleDepartments = (values: string[]) => {
        setInputValues({...inputValues, departments: [...values]})
    }

    const saveParticipant = () => {
        const errors = ValidateCourseParticipantForm(inputValues);
        setInputErrors({...errors})

        if(errors.none){
            participant ? update(inputValues) : save(inputValues);
            closeModal()
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
            <div className='w-full mt-5'>
                <Scroll>
                    <form className='flex flex-col gap-3'>
                        <motion.div 
                            className="w-full"
                            initial={{ opacity: 0, x: -50 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ duration: 0.3, ease: "easeOut", delay: 0.2 }}
                        >
                            <Dropdown
                                save={handleLevels}
                                placeHolder='Level'
                                options={["100", "200", "300", "400", "500"]}
                                value={inputValues?.level}
                                error={inputErrors.level}
                            />
                        </motion.div>
                        <motion.div 
                            className="w-full"
                            initial={{ opacity: 0, x: -50 }}
                            animate={{ opacity: 1, x: 0 }}
                            transition={{ duration: 0.3, ease: "easeOut", delay: 0.4 }}
                        >
                            <ListInput
                                save={handleDepartments}
                                saveOptions={saveDepartments}
                                placeHolder='Search department...'
                                options={dropDownOptions.departments}
                                searchPlaceHolder='Search Department..'
                                canSearch={true}
                                isSearch={true}
                                optionsApiRoute={ApiRoutes.DEPARTMENT.filter}
                                inputValuesList={inputValues?.departments}
                                isDropdown={true}
                                emptyText='departments'
                                error={inputErrors.departments}
                            />
                        </motion.div>
                        <Button
                            processing={false}
                            onClick={saveParticipant}
                            styleClass='bg-bowen-blue hover:border-bowen-blue text-white font-semibold'
                            spinnerStyleClass='w-6 h-6 mx-auto mt-6'
                            delay={0.6}
                        >
                            {participant ? "Update Participant" : "Add Participant"}
                        </Button>
                    </form>
                </Scroll>
            </div>
        </div>
    )
}

export default CourseParticipantForm