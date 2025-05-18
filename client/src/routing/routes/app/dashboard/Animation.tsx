import React, { useState, useEffect, useCallback, useMemo } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { ComponentReveal } from "../../../../ui/animations/ComponentAnimations";
import { GenericObjectType } from "../../../../utils/Types";
import { useApi, useAuth } from "../../../../hooks/useContexts";
import logo from '../../../../assets/images/logo.svg'
import ApiRoutes from "../../../../utils/ApiRoutes";
import Spinner from "../../../../ui/Spinner";

const Animation:React.FC = () =>{
    const [index, setIndex] = useState(0);
    const [events, setEvents] = useState<GenericObjectType[]>([])

    const { makeGetRequest } = useApi()
    const { user } = useAuth()

    const [loading, setLoading] = useState<boolean>(true);

    const isEmpty = useMemo(() => events.length === 0,[events])

    const getSchedules = useCallback(async () => {
        const { success, responseBody } = await makeGetRequest(ApiRoutes.SCHEDULE.get, {userId: user?.id});
        if(success){
            setEvents([...responseBody!.result])
        }
        setLoading(false)
    }, [])

    useEffect(() => {
        let interval:NodeJS.Timer;
        if(!isEmpty){
            interval = setInterval(() => {
                if(!isEmpty) setIndex((prev) => (prev + 1) % events.length);
            }, 5000);
        }
        return () => clearInterval(interval);
    }, [isEmpty]);

    useEffect(() => {
        getSchedules()
    },[getSchedules])

    return (
        <div className=" items-center w-full h-full relative">
            {
                loading ? 
                    <Spinner spinnerStyleClass='w-12 aspect-square top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 absolute'/> 
                :
                !loading && isEmpty ?
                    <p className='text-center aspect-square top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 absolute'>
                        No Schedules available...
                    </p>
                : 
                <AnimatePresence mode="wait">
                    <motion.div
                        key={index}
                        initial={{ rotateY: 90, opacity: 0 }}
                        animate={{ rotateY: 0, opacity: 1 }}
                        exit={{ rotateY: -90, opacity: 0 }}
                        transition={{ duration: 1.2 }}
                        className="w-full h-full bg-white dark:bg-sidebar flex flex-col gap-4 items-center justify-center shadow-lg rounded-xl p-4 text-center transform-style-3d"
                    >
                        <ComponentReveal
                            type='image'
                            src={logo} 
                            alt="" 
                            styleClass="w-28 aspect-square"
                            delay={0.2}
                        />
                        <h2 className="text-5xl text-white font-semibold">{events[index]?.course?.courseCodes?.join(", ")}</h2>
                        <p className="text-sm text-gray-600 dark:text-gray-300">{events[index]?.time} - {events[index]?.classrooms?.map((room: GenericObjectType) => room?.className).join(", ")}</p>
                    </motion.div>
                </AnimatePresence>
            }
        </div>
    );
}

export default Animation;