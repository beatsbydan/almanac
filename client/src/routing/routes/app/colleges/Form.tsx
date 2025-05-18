import React, { useState, useEffect } from 'react'
import { useApi } from '../../../../hooks/useContexts';
import { GenericObjectType, ModalFormType } from '../../../../utils/Types';
import { alertFailure, alertSuccess } from '../../../../utils/Notifications';
import { ValidateCollegeForm } from '../Validations';
import { X } from 'lucide-react'
import { Input } from '../../../../ui/Input';
import Button from '../../../../ui/Button';
import ApiRoutes from '../../../../utils/ApiRoutes';

const Form:React.FC<ModalFormType & {closeModal: ()=>void, reFetchMethod: () => void}> = ({closeModal, inputValues: inputFields, reFetchMethod}) => {
    const { makePostRequest, makePutRequest } = useApi();

    useEffect(() => {
        if(inputFields){
            setInputValues({...inputFields})
        }
    }, [inputFields])

    const [inputValues, setInputValues] = useState<GenericObjectType>({
        collegeName: "",
        collegeShortName: ""
    })

    const [inputErrors, setInputErrors] = useState<GenericObjectType>({
        collegeName: {
            valid: false,
            message: ""
        },
        collegeShortName: {
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
            collegeName: "",
            collegeShortName: ""
        })
        setInputErrors({
            collegeName: {
                valid: false,
                message: ""
            },
            collegeShortName: {
                valid: false,
                message: ""
            },
            none: false
        })
    }

    const handleSubmit = async () => {
        const errors = ValidateCollegeForm(inputValues);
        setInputErrors(errors);

        if(errors.none){
            setProcessing(true);
            const { success, error, responseBody } = inputFields ? await makePutRequest (
                ApiRoutes.COLLEGE.update + inputFields?.code,
                inputValues,

            ) : await makePostRequest(
                ApiRoutes.COLLEGE.create,
                inputValues
            )

            if(success){
                alertSuccess(responseBody!.result.message)
                clearInputFields()
                closeModal()
                reFetchMethod()
            }
            if(error){
                alertFailure(error!.result.message)
            }
            setProcessing(false)
        }
    }

    return (
        <div className='w-full h-[calc(83vh-250px)] relative'>
            <X
                className="cursor-pointer right-0 absolute -top-8 text-light-gray"
                onClick={closeModal}
            />

            <form className='w-full flex flex-col gap-3 mt-7 mb-5 items-end'>
                <Input
                    name="collegeName"
                    type="text"
                    placeholder="College of College"
                    label="College Name"
                    error={inputErrors.collegeName}
                    value={inputValues.collegeName}
                    onChange={handleChange}
                    delay={0.2}
                />
                <Input
                    name="collegeShortName"
                    type="text"
                    placeholder="SHORT_NAME"
                    label="College Short Name"
                    error={inputErrors.collegeShortName}
                    value={inputValues.collegeShortName}
                    onChange={handleChange}
                    delay={0.4}
                />
            </form>
            <Button
                processing={processing}
                onClick={handleSubmit}
                styleClass='bg-bowen-blue hover:border-bowen-blue text-white font-semibold mt-5'
                spinnerStyleClass='w-6 h-6 mx-auto'
                delay={0.6}
            >
                {inputFields ? "Update" : "Create"}
            </Button>
        </div>
    )
}

export default React.memo(Form)