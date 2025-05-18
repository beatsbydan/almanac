import React, { useMemo } from 'react'
import { TableType } from '../../utils/Types'
import { ComponentReveal } from '../animations/ComponentAnimations'
import Spinner from '../Spinner'
import Scroll from '../Scroll'
import Row from './Row'

const Table:React.FC<TableType> = ({
    list,
    displayKeys,
    emptyText,
    loading,
    headers, 
    canDelete,
    delay,
    canEdit,
    listStyle,
    isFilterable,
    tableHeight,
    deleteApiRoute,
    selectEntity,
    refetchMethod,
}) => {
    const isEmpty = useMemo(() => {
        return list.length === 0
    }, [list])

    return (
        <ComponentReveal
            styleClass=''
            delay={delay || 1.2}
            type="container"
        >
            <div className='mt-5 w-full shadow-2xl rounded-b-lg'>
                <ul className={"grid w-full bg-sidebar text-white py-3 rounded-t-lg " + listStyle}>
                    {headers.map((header, index)=>{
                        const isLast = index === (headers.length - 1);
                        return (
                            <li key={index} className={`${!isLast ? "text-center font-bold border-r-1" : "text-center font-bold"}`}>
                                {header}
                            </li>
                        )
                    })}
                </ul>
                <ul className={`${tableHeight || 'h-[61vh]'} relative` }>
                    {
                        loading ? 
                            <Spinner spinnerStyleClass='w-12 aspect-square top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 absolute'/> 
                        :
                        !loading && isEmpty ?
                            <li className='text-center aspect-square top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 absolute'>
                                {`${isFilterable ? `Filter to see ${emptyText}...` : `No ${emptyText} available..`}`}
                            </li>
                        : 
                        <Scroll>
                            {list.map((entity, index) => {
                                return (
                                    <Row 
                                        key={index}
                                        index={index}
                                        listStyle={listStyle}
                                        entity={entity}
                                        displayKeys={displayKeys}
                                        canEdit={canEdit}
                                        canDelete={canDelete}
                                        deleteApiRoute={deleteApiRoute}
                                        selectEntity={selectEntity}
                                        refetchMethod={refetchMethod}
                                    />
                                )
                            })}
                        </Scroll>
                    }
                </ul>
            </div>
        </ComponentReveal>
    )
}

export default Table