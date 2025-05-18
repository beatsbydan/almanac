import React, { useMemo } from 'react'
import { ToggleType } from '../utils/Types'

const Toggle:React.FC<ToggleType> = ({styleClass: style, action, color, condition}) => {
    const toggledBg = useMemo(() => {
        return !condition ? 'bg-transparent' : `${color}`
    } ,[condition, color])

    const animateToggle = useMemo(() => {
        return condition ? " animate-toggle-on " : " animate-toggle-off "
    }, [condition])

    return (
        <div className={"w-full max-w-[100px] p-1 border-black border-1 shadow-md rounded-3xl " + toggledBg}>
            <div 
                className={"rounded-full border-black border-1 bg-white cursor-pointer " + style + animateToggle}
                onClick={action}
            />
        </div>
    )
}

export default React.memo(Toggle)