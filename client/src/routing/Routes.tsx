import React from 'react'
import { Routes as AppRouting, Route, Navigate } from 'react-router-dom'
import { AdminPrivileges, StaffPrivileges } from './Privileges'
import Login from './routes/auth/Login'
import Register from './routes/auth/register/Register'
import VerifyEmail from './routes/auth/VerifyEmail'
import VerifyOtp from './routes/auth/VerifyOtp'
import ResetPassword from './routes/auth/ResetPassword'
import ReAuth from './ReAuth'
import Dashboard from './routes/app/dashboard/Dashboard'
import Courses from './routes/app/courses/Courses'
import ClassRooms from './routes/app/classrooms/ClassRooms'
import Schedules from './routes/app/schedules/Schedules'
import Timetables from './routes/app/timetables/Timetables'
import Colleges from './routes/app/colleges/Colleges'
import Departments from './routes/app/departments/Departments'

const Routes:React.FC = () => {
    return (
        <AppRouting>

            {/* ONBOARDING */}
            <Route path = "/" element={<Navigate to="/login"/>}/>
            <Route path = "/login" element={<Login/>}/>
            <Route path = "/register" element={<Register/>}/>
            <Route path = "/verify-email" element={<VerifyEmail/>}/>
            <Route path = "/verify-otp" element={<VerifyOtp/>}/>
            <Route path = "/reset-password" element={<ResetPassword/>}/>

            {/* DEFAULT-NOT_FOUND */}
            <Route path="*" element={<Navigate to="/login"/>}/>

            {/* APPLICATION */}
            <Route element={<ReAuth/>}>
                <Route element={<StaffPrivileges/>}>
                    <Route path="/dashboard" element={<Dashboard/>}/>
                    <Route path="/schedules" element={<Schedules/>}/>
                </Route>

                <Route element={<AdminPrivileges/>}>
                    <Route path="/courses" element={<Courses/>}/>
                    <Route path="/classrooms" element={<ClassRooms/>}/>
                    <Route path="/departments" element={<Departments/>}/>
                    <Route path="/colleges" element={<Colleges/>}/>
                </Route>
                
                <Route path="/timetables" element={<Timetables/>}/>
            </Route>
        </AppRouting>
    )
}

export default Routes