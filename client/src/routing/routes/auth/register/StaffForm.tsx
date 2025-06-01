import React from 'react'
import { RegisterFormType } from '../../../../utils/Types'
import { Input } from '../../../../ui/Input'
import { motion } from 'framer-motion'
import Dropdown from '../../../../ui/Dropdown'

const StaffForm:React.FC<RegisterFormType> = ({
    inputValues, 
    inputErrors, 
    handleChange, 
    handleTitleChange,
}) => {
    return (
        <div className='w-full flex flex-col gap-3 mb-5 items-end'>
            <motion.div 
                className="mt-3 w-full"
                initial={{ opacity: 0, x: -50 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.3, ease: "easeOut", delay: 0.8 }}
            >
                <Dropdown
                    save={handleTitleChange}
                    placeHolder='Select Title'
                    options={[
                        "Mr.",
                        "Mrs.",
                        "Dr.",
                        "Prof."
                    ]}
                    value={inputValues.title}
                    error={inputErrors.title}
                />
            </motion.div>
            <Input
                name="firstName"
                type="text"
                placeholder="John"
                label="First Name"
                error={inputErrors.firstName}
                value={inputValues.firstName}
                onChange={handleChange}
                delay={1.0}
            />
            <Input
                name="lastName"
                type="text"
                placeholder="Doe"
                label="Last Name"
                error={inputErrors.lastName}
                value={inputValues.lastName}
                onChange={handleChange}
                delay={1.2}
            />
            <Input
                name="userEmail"
                type="text"
                placeholder="example@bowen.edu.ng"
                label="Email"
                error={inputErrors.userEmail}
                value={inputValues.userEmail}
                onChange={handleChange}
                delay={1.4}
            />
            <Input
                name="userId"
                type="text"
                placeholder="BU-STAFF-ID"
                label="Staff ID"
                error={inputErrors.userId}
                value={inputValues.userId}
                onChange={handleChange}
                delay={1.6}
            />
            <Input
                name="phone"
                type="text"
                placeholder="+23400000000"
                label="Phone"
                error={inputErrors.phone}
                value={inputValues.phone}
                onChange={handleChange}
                delay={1.8}
            />
            <Input
                name="password"
                type="password"
                placeholder="* * * * * * * *"
                label="Password"
                error={inputErrors.password}
                value={inputValues.password}
                onChange={handleChange}
                delay={2.0}
            />
            <Input
                name="confirmPassword"
                type="password"
                placeholder="* * * * * * * *"
                label="Confirm password"
                error={inputErrors.confirmPassword}
                value={inputValues.confirmPassword}
                onChange={handleChange}
                delay={2.2}
            />
        </div>
    )
}

export default StaffForm