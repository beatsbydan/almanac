import React from 'react'
import { useNavigate } from 'react-router-dom'
import { DashboardRouteType } from '../../../../utils/Types'
import { House, ListTodo, TableCellsMerge, School, BookOpen, GraduationCap } from 'lucide-react'
import { ComponentReveal } from '../../../../ui/animations/ComponentAnimations'
import { AdminPermission } from '../../../Permissions'

const Route:React.FC<DashboardRouteType> = ({path, description, children}) => {
    const navigate = useNavigate()
    return (
        <div
            className="flex items-center gap-2 rounded-lg shadow-sm p-3 bg-white transition-colors duration-500 ease-in-out cursor-pointer hover:text-white hover:bg-sidebar border-1 border-sidebar hover:border-white"
            onClick={()=>navigate(path)}
        >
            <div>{children}</div>
            <span>{description}</span>
        </div>
    )
}

const Routes:React.FC = () => {
    return (
        <div className="w-full flex items-center gap-4 mt-3">
            <AdminPermission>
                <ComponentReveal
                    type="container"
                    delay={0.2}
                    styleClass=''
                >
                    <Route
                        path="/colleges"
                        description="Colleges"
                    >
                        <School/>
                    </Route>
                </ComponentReveal>
            </AdminPermission>
            <AdminPermission>
                <ComponentReveal
                    type="container"
                    delay={0.4}
                    styleClass=''
                >
                    <Route
                        path="/classrooms"
                        description="Classrooms"
                    >
                        <House/>
                    </Route>
                </ComponentReveal>
            </AdminPermission>
            <AdminPermission>
                <ComponentReveal
                    type="container"
                    delay={0.6}
                    styleClass=''
                >
                    <Route
                        path="/departments"
                        description="Departments"
                    >
                        <GraduationCap/>
                    </Route>
                </ComponentReveal>
            </AdminPermission>
            <AdminPermission>
                <ComponentReveal
                    type="container"
                    delay={0.8}
                    styleClass=''
                >
                    <Route
                        path="/courses"
                        description="Courses"
                    >
                        <BookOpen/>
                    </Route>
                </ComponentReveal>
            </AdminPermission>
            <ComponentReveal
                type="container"
                delay={1.0}
                styleClass=''
            >
                <Route
                    path="/schedules"
                    description="My Schedules"
                >
                    <ListTodo/>
                </Route>
            </ComponentReveal>
            <ComponentReveal
                type="container"
                delay={1.2}
                styleClass=''
            >
                <Route
                    path="/timetables"
                    description="Timetables"
                >
                    <TableCellsMerge/>
                </Route>
            </ComponentReveal>
        </div>
    )
}

export default Routes