import React, { useEffect, useMemo } from 'react'
import { useAuth, useNav } from '../../../../hooks/useContexts'
import { ROLES } from '../../../../utils/Constants';
import { ComponentReveal } from '../../../../ui/animations/ComponentAnimations';
import { getSession } from '../../../../utils/Helpers';
import Card from '../Card'
import Routes from './Routes';
import Admin from './Admin';
import Lecturer from './Lecturer';
import { LecturerPermission } from '../../../Permissions';

const Dashboard:React.FC = () => {
    const { changeCurrentPage } = useNav();
    const { role } = useAuth()

    useEffect(() => { 
        changeCurrentPage("Dashboard")
    },[changeCurrentPage])

    const isAdmin = useMemo(() => {
        return role === ROLES.ADMIN
    },[role])
    
    return (
        <Card>
            <div className = "w-full">
                <div className='flex justify-between items-center'>
                    <Routes/>
                    <LecturerPermission>
                        <ComponentReveal 
                            type='text'
                            styleClass='text-5xl mt-6 font-semibold text-light-gray text-right'
                            delay={0.5}
                        >
                            {getSession()}
                        </ComponentReveal>  
                    </LecturerPermission>
                </div>
                <div className="w-full -mt-2">
                    {isAdmin ? <Admin/> : <Lecturer/>}
                </div>
            </div>
        </Card>
    )
}

export default Dashboard