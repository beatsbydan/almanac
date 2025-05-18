import React, { useState } from 'react'
import { Input } from '../../../../ui/Input'
import { GenericObjectType, RegisterFormType } from '../../../../utils/Types'
import { motion } from 'framer-motion'
import Dropdown from '../../../../ui/Dropdown'
import ApiRoutes from '../../../../utils/ApiRoutes'

const StudentForm:React.FC<RegisterFormType> = ({
    inputValues, 
    inputErrors, 
    handleChange, 
    handleLevelChange, 
    handleDepartmentChange
}) => {
    const [dropDownOptions, setDropDownOptions] = useState<GenericObjectType>({
        departments: [],
        levels: [
            "100", 
            "200", 
            "300", 
            "400", 
            "500"
        ]
    })
    const saveDepartments = (fetchedDepartments: GenericObjectType[]) => {
        const filteredDepartments = fetchedDepartments.map(department => department?.departmentName)
        setDropDownOptions({...dropDownOptions, departments: [...filteredDepartments]})
    }
    
    return (
        <form className='w-full flex flex-col gap-3 mb-5 items-end'>
            <Input
                name="userEmail"
                type="text"
                placeholder="lastname.firstname@bowen.edu.ng"
                label="Email"
                error={inputErrors.userEmail}
                value={inputValues.userEmail}
                onChange={handleChange}
                delay={0.8}
            />
            <Input
                name="userId"
                type="text"
                placeholder="BU-STUDENT-ID"
                label="Student ID"
                error={inputErrors.userId}
                value={inputValues.userId}
                onChange={handleChange}
                delay={1.0}
            />
            <motion.div 
                className="mt-3 w-full"
                initial={{ opacity: 0, x: -50 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.3, ease: "easeOut", delay: 1.2 }}
            >
                <Dropdown
                    save={handleDepartmentChange}
                    saveOptions={saveDepartments}
                    placeHolder='Search department'
                    options={dropDownOptions.departments}
                    value={inputValues.department}
                    searchPlaceholder='Search Department..'
                    canSearch={true}
                    searchOnly={true}
                    optionsApiRoute={ApiRoutes.DEPARTMENT.filter}
                    error={inputErrors.department}
                />
            </motion.div>
            <motion.div 
                className="mt-3 w-full"
                initial={{ opacity: 0, x: -50 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.3, ease: "easeOut", delay: 1.4 }}
            >
                <Dropdown
                    save={handleLevelChange}
                    placeHolder='Level'
                    options={dropDownOptions.levels}
                    value={inputValues.level}
                    error={inputErrors.level}
                />
            </motion.div>
            <Input
                name="password"
                type="password"
                placeholder="* * * * * * * *"
                label="Password"
                error={inputErrors.password}
                value={inputValues.password}
                onChange={handleChange}
                delay={1.6}
            />
            <Input
                name="confirmPassword"
                type="password"
                placeholder="* * * * * * * *"
                label="Confirm password"
                error={inputErrors.confirmPassword}
                value={inputValues.confirmPassword}
                onChange={handleChange}
                delay={1.8}
            />
        </form>
    )
}

export default StudentForm