import React, { useState } from 'react'
import { FilterType, FilterParamsType, GenericObjectType } from '../utils/Types';
import { X } from 'lucide-react';
import { motion } from 'framer-motion'
import { Input, RadioButtonInput } from './Input';
import { DaysOfTheWeek } from '../utils/Constants';
import { useAuth } from '../hooks/useContexts';
import Dropdown from './Dropdown'
import Button from './Button';
import ApiRoutes from '../utils/ApiRoutes';

export const CollegeFilter:React.FC<FilterType> = React.memo(({setFilter}) => {
    const [colleges, setColleges] = useState<GenericObjectType[]>([])
    const saveColleges = (e: GenericObjectType[]) => {
        const mappedColleges = e.map(college => college?.collegeShortName)
        setColleges([...mappedColleges, "NONE"])
    }

    return (
        <Dropdown
            placeHolder='College'
            options={colleges}
            save={setFilter}
            optionsApiRoute={ApiRoutes.COLLEGE.get}
            saveOptions={saveColleges}
        />
    )
})

export const CourseFilter:React.FC<FilterParamsType & {closeModal: ()=>void}> = React.memo(({setFilter, closeModal}) => {
    const [filterParams, setFilterParams] = useState<GenericObjectType>({
        courseUnit: null,
        department: null,
        level: null,
        isShared: false,
        isBatched: false,
        isGeneral: false,
    })
    
    const [dropDownOptions, setDropDownOptions] = useState<GenericObjectType>({
        departments: []
    })

    const [inputErrors] = useState<GenericObjectType>({
        department: {
            valid: false,
            message: ""
        },
        courseUnit: {
            valid: false,
            message: ""
        },
        level: {
            valid: false,
            message: ""
        },
        none: false
    })

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        const {name, value} = e.target;
        setFilterParams(prev => {
            return {...prev, [name]: value}
        })
    }

    const saveDepartments = (fetchedDepartments: GenericObjectType[]) => {
        const filteredDepartments = fetchedDepartments.map(department => department?.departmentName)
        setDropDownOptions({...dropDownOptions, departments: [...filteredDepartments]})
    }
    
    const handleLevelChange =(e: string) => {
        setFilterParams({...filterParams, level: e})
    }
    const handleDepartmentChange = (e: string) => {
        setFilterParams({...filterParams, department: e})
    }
    const handleSharedChange = () => {
        setFilterParams({...filterParams, isShared: !filterParams.isShared})
    }
    const handleBatchedChange = () => {
        setFilterParams({...filterParams, isBatched: !filterParams.isBatched})
    }
    const handleGeneralChange = () => {
        setFilterParams({...filterParams, isGeneral: !filterParams.isGeneral})
    }

    const save = () => {
        setFilter({...filterParams})
        closeModal()
    }

    return (
        <div className='w-full relative'>
            <X
                className="cursor-pointer right-0 absolute -top-6 text-light-gray"
                onClick={closeModal}
            />
            <p className='font-semibold mt-3 text-[24px]'>Filter</p>
            <form className='w-full flex flex-col gap-3 mb-5 items-end h-full'>
                <motion.div 
                    className="mt-3 w-full"
                    initial={{ opacity: 0, x: -50 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.3, ease: "easeOut", delay: 0.4 }}
                >
                    <Dropdown
                        save={handleDepartmentChange}
                        saveOptions={saveDepartments}
                        placeHolder='Department'
                        searchPlaceholder='Enter Department...'
                        options={dropDownOptions.departments}
                        canSearch={true}
                        searchOnly={true}
                        optionsApiRoute={ApiRoutes.DEPARTMENT.filter}
                        value={filterParams?.department}
                    />
                </motion.div>
                <motion.div 
                    className="mt-3 w-full"
                    initial={{ opacity: 0, x: -50 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ duration: 0.3, ease: "easeOut", delay: 0.2 }}
                >
                    <Dropdown
                        save={handleLevelChange}
                        canSearch={false}
                        value={filterParams?.level}
                        placeHolder='levels'
                        options={["100", "200", "300", "400", "500"]}
                    />
                </motion.div>
                <Input
                    name="courseUnit"
                    type="text"
                    placeholder="0"
                    label="Course Unit"
                    error={inputErrors.courseUnit}
                    value={filterParams.courseUnit}
                    onChange={handleChange}
                    delay={0.4}
                />
                <div className='w-full flex items-center justify-center gap-5 mt-5'>
                    <RadioButtonInput
                        handleChange={handleBatchedChange}
                        value={filterParams.isBatched}
                        label={"Batched course"}
                        delay={0.8}
                    />
                    <RadioButtonInput
                        handleChange={handleSharedChange}
                        value={filterParams.isShared}
                        label={"Shared course"}
                        delay={1.0}
                    />
                    <RadioButtonInput
                        handleChange={handleGeneralChange}
                        value={filterParams.isGeneral}
                        label={"General course"}
                        delay={1.2}
                    />
                </div>
            </form>
            <Button
                processing={false}
                onClick={save}
                styleClass='bg-bowen-blue hover:border-bowen-blue text-white font-semibold mt-5'
                spinnerStyleClass='w-6 h-6 mx-auto'
                delay={1.4}
            >
                Apply
            </Button>
        </div>
    )
})

export const ScheduleFilter:React.FC<FilterParamsType & {closeModal: ()=>void}> = React.memo(({setFilter, closeModal}) => {
    const { user } = useAuth()
    const [filterParams, setFilterParams] = useState<GenericObjectType>({
        userId: user?.id,
        day: null
    })
    
    const handleDayChange = (e: string) => {
        setFilterParams({...filterParams, day: e})
    }
    
    const save = () => {
        setFilter({...filterParams})
        closeModal()
    }

    return (
        <div className='w-full relative'>
            <X
                className="cursor-pointer right-0 absolute -top-6 text-light-gray"
                onClick={closeModal}
            />
            <p className='font-semibold mt-3 text-[24px]'>Filter</p>
            <form className='w-full flex flex-col gap-3 mb-5 items-end h-full'>
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
                        value={filterParams.day}
                    />
                </motion.div>
            </form>
            <Button
                processing={false}
                onClick={save}
                styleClass='bg-bowen-blue hover:border-bowen-blue text-white font-semibold mt-5'
                spinnerStyleClass='w-6 h-6 mx-auto'
                delay={0.6}
            >
                Apply
            </Button>
        </div>
    )
})

export const SuggestionFilter:React.FC = () => {
    return (
        <div>
            
        </div>
    )
}