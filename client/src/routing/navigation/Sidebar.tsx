import React, { useState, useMemo } from 'react'
import { NavLink as Link, useLocation } from 'react-router-dom'
import { ComponentReveal } from '../../ui/animations/ComponentAnimations'
import { LayoutDashboard, House, ListTodo, TableCellsMerge, BookOpen, School, GraduationCap } from 'lucide-react'
import { NavLinkType } from '../../utils/Types'
import { motion } from 'framer-motion'
import logo from '../../assets/images/logo.svg'
import { AdminPermission, StaffPermission } from '../Permissions'

const NavLink:React.FC<NavLinkType> = ({children, path, name, initialTopMargin, delay}) => {
  const location = useLocation();

  const [isOpen, setOpen] = useState<boolean>(false);

  const isActive = useMemo(() => {
    return location.pathname === path;
  }, [location, path])

  const styleClass = useMemo(() => {
    return isActive ? "bg-white" : "text-white"
  }, [isActive])
  
  return (
    <motion.div 
      initial={{ opacity: 0, y: initialTopMargin }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3, ease: "easeOut", delay: delay }}
      onMouseEnter={()=>setOpen(true)}
      onMouseLeave={()=>setOpen(false)}
      className={'relative flex rounded-sm justify-center items-center shadow-2xl hover:bg-white  hover:text-black transition-colors ease-out duration-500 ' + styleClass}
    >
      <Link 
        to={path}
        className="w-full p-2 flex justify-center "
      >
        {children}
      </Link>
      { isOpen &&
        <div className="absolute left-[calc(100%+30px)] flex items-center justify-center">
          <motion.div
            initial={{ opacity: 0, scale: 0.8, x: -40 }}
            animate={{ opacity: 1, scale: 1, x: 0 }}
            transition={{ duration: 0.3, ease: "easeOut" }}
            className="relative bg-black px-4 py-2 rounded-md shadow-lg"
          >
            <div className="absolute -left-[8px] top-1/2 -translate-y-1/2 border-y-12 border-r-12 border-l-0 border-solid border-transparent border-r-black"/>
            <p className='text-white text-center'>{name}</p>
          </motion.div>
        </div>
      }
    </motion.div>
  )
}

const Sidebar:React.FC = () => {
  return (
    <motion.div
      initial={{ opacity: 0, x: -40 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ duration: 0.3, ease: "easeOut" }} 
      className='w-full max-w-[100px] p-4 h-screen bg-sidebar fixed z-30'>
      <ComponentReveal
        type='image'
        src={logo} 
        alt="" 
        styleClass="w-12 h-12 mx-auto"
        delay={0.2}
      />
      <nav className='w-full my-5 flex flex-col gap-4'>
        <StaffPermission>
          <NavLink
            path="/dashboard"
            name="Dashboard"
            initialTopMargin={20}
            delay={0.3}
          >
            <LayoutDashboard/>
          </NavLink>
        </StaffPermission>

        <AdminPermission>
          <NavLink
            path="/colleges"
            name="Colleges"
            initialTopMargin={20}
            delay={0.4}
          >
            <School/>
          </NavLink>
        </AdminPermission>

        <AdminPermission>
          <NavLink
            path="/departments"
            name="Departments"
            initialTopMargin={20}
            delay={0.5}
          >
            <GraduationCap/>
          </NavLink>
        </AdminPermission>

        <AdminPermission>
          <NavLink
            path="/classrooms"
            name="Classrooms"
            initialTopMargin={40}
            delay={0.6}
          >
            <House/>
          </NavLink>
        </AdminPermission>

        <AdminPermission>
          <NavLink
            path="/courses"
            name="Courses"
            initialTopMargin={60}
            delay={0.7}
          >
            <BookOpen/>
          </NavLink>
        </AdminPermission>

        <StaffPermission>
          <NavLink
            path="/schedules"
            name="My Schedules"
            initialTopMargin={80}
            delay={0.8}
          >
            <ListTodo/>
          </NavLink>
        </StaffPermission>

        <NavLink
          path="/timetables"
          name="Timetables"
          initialTopMargin={100}
          delay={0.7}
        >
          <TableCellsMerge/>
        </NavLink>
      </nav>
    </motion.div>
  )
}

export default Sidebar