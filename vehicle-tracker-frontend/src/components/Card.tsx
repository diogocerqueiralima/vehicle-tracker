import React from "react";

export enum Indicator {

    ONLINE,
    OFFLINE

}

export enum ChangeType {

    POSITIVE,
    NEGATIVE

}

export interface CardProps {

    title: string,
    label: string,
    indicator: Indicator,
    icon: React.ReactNode,
    changeType: ChangeType,
    change: string

}

function getIndicatorColor(indicator: Indicator) {

    switch (indicator) {
        case Indicator.ONLINE:
            return "bg-positive";
        case Indicator.OFFLINE:
            return "bg-negative";
    }

}

function getChangeColor(changeType: ChangeType) {

    switch (changeType) {
        case ChangeType.POSITIVE:
            return "text-positive";
        case ChangeType.NEGATIVE:
            return "text-negative";
    }

}

export default function Card({ title, label, indicator, icon, change, changeType } : CardProps) {
    return (
        <div className={`flex flex-col items-start gap-4 h-fit w-fit bg-surface rounded-xl p-8 shadow-md`}>

            <div className={`flex flex-row items-start gap-8`}>

                <div className={`flex flex-col items gap-2`}>

                    <div className={`flex flex-row gap-2 items-center`}>
                        <span className={`${getIndicatorColor(indicator)} h-2 w-2 rounded-full`}></span>
                        <span className={`text-lg text-foreground opacity-60`}>{title}</span>
                    </div>

                    <span className={`text-4xl text-foreground`}>{label}</span>
                </div>

                <div className={`bg-background text-foreground opacity-60 p-4 rounded-xl`}>
                    { icon }
                </div>

            </div>

            <span className={`${getChangeColor(changeType)} text-lg`}>{change}</span>

        </div>
    )
}