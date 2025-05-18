import React, { useEffect } from 'react'
import { ComponentReveal } from './animations/ComponentAnimations';
import useGetCountdown from '../hooks/useGetCountdown';

const Countdown:React.FC<{nextDate: string | number | Date, delay: number}> = ({nextDate, delay}) => {
    const UNIT_CONTAINER_STYLE = "flex flex-col gap-2 justify-center items-center";
    const UNIT_STYLE = "flex items-center gap-1";
    const UNIT_ELEMENT_STYLE= "text-2xl font-bold px-3 py-2 bg-white border-black border-[0.1rem] rounded-lg";

    const { countDownInfo, getCountDownInfo } = useGetCountdown();

    useEffect(() => {
        getCountDownInfo(nextDate);

        const interval = setInterval(()=>{
            getCountDownInfo(nextDate);
        }, 60000);

        return () => clearInterval(interval)
    },[getCountDownInfo])
    
    return (
        <div className=" absolute bottom-0 right-0 mt-16 text-center lg- pb-12 md:pb-0">
            <ComponentReveal 
                styleClass="font-bold text-sidebar text-sm"
                type="text"
                delay={delay}
            >
                TIME REMAINING
            </ComponentReveal>
            <div className="w-full max-w-[450px] mx-auto lg:mx-0 lg:max-w-full flex gap-4 mt-2">
                <div className={UNIT_CONTAINER_STYLE}>
                    <div className={UNIT_STYLE}>
                        <ComponentReveal  
                            styleClass={UNIT_ELEMENT_STYLE}
                            type="container"
                            delay={delay + 0.2}
                        >
                            {countDownInfo?.days?.split("")[0]}
                        </ComponentReveal>
                        <ComponentReveal  
                            styleClass={UNIT_ELEMENT_STYLE}
                            type="container"
                            delay={delay + 0.3}
                        >
                            {countDownInfo?.days?.split("")[1]}
                        </ComponentReveal>
                    </div>
                    <ComponentReveal 
                        styleClass="font-semibold text-light-gray"
                        type="text"
                        delay={delay + 0.4}
                    >
                        Days
                    </ComponentReveal>
                </div>
                <div className={UNIT_CONTAINER_STYLE}>
                    <div className={UNIT_STYLE}>
                        <ComponentReveal  
                            styleClass={UNIT_ELEMENT_STYLE}
                            type="container"
                            delay={delay + 0.5}
                        >
                            {countDownInfo?.hours?.split("")[0]}
                        </ComponentReveal>
                        <ComponentReveal  
                            styleClass={UNIT_ELEMENT_STYLE}
                            type="container"
                            delay={delay + 0.6}
                        >
                            {countDownInfo?.hours?.split("")[1]}
                        </ComponentReveal>
                    </div>
                    <ComponentReveal 
                        styleClass="font-semibold text-light-gray"
                        type="text"
                        delay={delay + 0.7}
                    >
                        Hours
                    </ComponentReveal>
                </div>
                <div className={UNIT_CONTAINER_STYLE}>
                    <div className={UNIT_STYLE}>
                        <ComponentReveal  
                            styleClass={UNIT_ELEMENT_STYLE}
                            type="container"
                            delay={delay + 0.8}
                        >
                            {countDownInfo?.minutes?.split("")[0]}
                        </ComponentReveal>
                        <ComponentReveal  
                            styleClass={UNIT_ELEMENT_STYLE}
                            type="container"
                            delay={delay + 0.9}
                        >
                            {countDownInfo?.minutes?.split("")[1]}
                        </ComponentReveal>
                    </div>
                    <ComponentReveal 
                        styleClass="font-semibold text-light-gray"
                        type="text"
                        delay={delay + 1.0}
                    >
                        Minutes
                    </ComponentReveal>
                </div>
            </div>
        </div>
    )
}

export default Countdown