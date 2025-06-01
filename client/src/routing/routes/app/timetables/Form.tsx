import React, { useState, useEffect } from 'react'
import { useApi } from '../../../../hooks/useContexts';
import { GenericObjectType, ModalFormType } from '../../../../utils/Types';
import { alertFailure, alertSuccess } from '../../../../utils/Notifications';
import { ValidateTimeTableForm } from '../Validations';
import { X } from 'lucide-react'
import { motion } from 'framer-motion'
import { DateInput } from '../../../../ui/Input';
import { getSession } from '../../../../utils/Helpers';
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
        semester: undefined,
        creationStartDate: "",
        creationEndDate: "",
    })

    const handleStartDateChange = (value: string) => {
        setInputValues({...inputValues, creationStartDate: value})
    }

    const handleEndDateChange = (value: string) => {
        setInputValues({...inputValues, creationEndDate: value})
    }

    const handleSemesterChange = (value: string) => {
        setInputValues({...inputValues, semester: value})
    }

    const [inputErrors, setInputErrors] = useState<GenericObjectType>({
        semester: {
            valid: false,
            message: ""
        },
        creationStartDate: {
            valid: false,
            message: ""
        },
        creationEndDate: {
            valid: false,
            message: ""
        },
        none: false
    })

    const [processing, setProcessing] = useState<boolean>(false);

    const clearInputFields = () => {
        setInputValues({
            semester: "",
            creationStartDate: "",
            creationEndDate: ""
        })
    }

    const handleSubmit = async () => {
        const errors = ValidateTimeTableForm(inputValues);
        setInputErrors(errors);

        if(errors.none){
            setProcessing(true);
            const { success, error, responseBody } = inputFields ? await makePutRequest (
                ApiRoutes.TIMETABLE.update + inputFields?.code,
                inputValues,

            ) : await makePostRequest(
                ApiRoutes.TIMETABLE.create,
                {...inputValues, session: getSession()}
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
            alertFailure("Some fields are still empty..")
        }
    }

    return (
        <div className='w-full relative'>
            <X
                className="cursor-pointer right-0 absolute -top-8 text-light-gray"
                onClick={closeModal}
            />

            <form className='w-full flex flex-col gap-3 mt-7 mb-5 items-end'>
                <p className='text-light-gray font-medium'>{`${getSession()} session`}</p>
                <motion.div 
                    className="mt-1 w-full"
                    initial={{ opacity: 0, x: -50 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.3, ease: "easeOut", delay: 0.6 }}
                >
                    <Dropdown
                        save={handleSemesterChange}
                        placeHolder='Semester'
                        options={["FIRST", "SECOND"]}
                        value={inputValues.semester}
                        error={inputErrors.semester}
                    />
                </motion.div>
                <DateInput
                    title='Creation start date'
                    placeholder='YYYY - MM - DD'
                    handleChange={handleStartDateChange}
                    value={inputValues.creationStartDate}
                    delay={0.8}
                    error={inputErrors.creationStartDate}
                />
                <DateInput
                    title='Creation end date'
                    placeholder='YYYY - MM - DD'
                    handleChange={handleEndDateChange}
                    value={inputValues.creationEndDate}
                    error={inputErrors.creationEndDate}
                    delay={1.0}
                />
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