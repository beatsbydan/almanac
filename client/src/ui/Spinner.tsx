import React from 'react'

const Spinner:React.FC<{spinnerStyleClass: string}> = ({spinnerStyleClass}) => {
    return (
        <div className={"rounded-full border-2 border-t-bowen-blue border-r-bowen-blue border-l-bowen-blue border-b-transparent animate-spin " + spinnerStyleClass}/>
    )
}

export default React.memo(Spinner)