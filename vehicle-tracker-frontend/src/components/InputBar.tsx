import React from "react";

interface InputBarProps {

    icon: React.ReactNode
    placeholder: string
    value: string
    onChange: (value: string) => void

}

export default function InputBar({ icon, placeholder, value, onChange }: InputBarProps) {

    return (

        <div className={`
            flex flex-row items-center gap-2 bg-background px-4 py-2 rounded-md
            transition duration-200 border border-surface-muted focus-within:border-highlight
        `}>

            { icon }

            <input
                placeholder={placeholder}
                value={value}
                onChange={e => onChange(e.target.value)}
                size={placeholder.length}
                className={`outline-none w-full`}
            />

        </div>

    )

}