"use client"

import React, {useState} from "react";
import {IoLogOut} from "react-icons/io5";
import Image from "next/image";
import Link from "next/link";
import {IoMdClose} from "react-icons/io";

interface Item {

    name: string
    href: string
    icon: React.ReactNode

}

interface Props {
    items: Item[]
}

export default function Header( { items } : Props ) {

    const [isOpen, setIsOpen] = useState(false)

    return (
        <header className={`
            md:sticky top-0 flex justify-center gap-4 md:gap-0 md:flex-col md:justify-between items-center bg-surface
            text-foreground md:h-screen py-10 px-4 transition-all duration-200 z-30
        `}>

            <div className="flex md:flex-col gap-12 items-center">

                { /* 1. Logo that open or close the sidebar */ }

                <Image
                    src="/Logo - 1000X1000.svg"
                    alt="MyTracker Logo"
                    width={64}
                    height={64}
                    className="cursor-pointer duration-200 hover:scale-105"
                    onClick={() => setIsOpen(!isOpen)}
                />

                { /* 2. Navigate Desktop items */ }

                <ul className={`hidden md:flex flex-col gap-4`}>

                    {

                        items.map((item, index) => (

                            <li key={index}>
                                <Link href={item.href}
                                    className={`
                                        flex items-center bg-background text-foreground px-8 py-2
                                        shadow-sm cursor-pointer rounded-lg hover:bg-highlight
                                        duration-200 hover:scale-105 w-full ${isOpen ? "gap-2" : "gap-0"}
                                    `}
                                    type="button"
                                >
                                    <span>{item.icon}</span>

                                    <div
                                        className="grid transition-[grid-template-columns] duration-200 ease-in-out"
                                        style={{
                                            gridTemplateColumns: isOpen ? "1fr" : "0fr",
                                        }}
                                    >

                                      <span className="overflow-hidden whitespace-nowrap text-xl">
                                        {item.name}
                                      </span>

                                    </div>
                                </Link>
                            </li>

                        ))

                    }

                </ul>

                { /* 3. Navigate Mobile items */ }

                <nav
                    className={`
                        md:hidden fixed top-0 left-0 h-screen w-screen z-50 
                        transform transition-transform duration-300
                        ${isOpen ? "bg-black/50 translate-x-0" : "-translate-x-full"}
                    `}
                >

                    <div
                        className={`
                            flex flex-col left-0 w-3/4 justify-between h-full items-center bg-surface px-8 py-16 z-20
                        `}
                    >

                        <ul className={`flex flex-col gap-4`}>

                            {

                                items.map((item, index) => (

                                    <li key={index}>
                                        <Link href={item.href}
                                              className={`
                                            flex items-center bg-background text-foreground px-8 py-2
                                            shadow-sm cursor-pointer rounded-lg hover:bg-highlight z-10
                                            duration-200 hover:scale-105 w-full ${isOpen ? "gap-2" : "gap-0"}
                                        `}
                                              type="button"
                                        >
                                            <span>{item.icon}</span>

                                            <div
                                                className="grid transition-[grid-template-columns] duration-200 ease-in-out"
                                                style={{
                                                    gridTemplateColumns: isOpen ? "1fr" : "0fr",
                                                }}
                                            >

                                          <span className="overflow-hidden whitespace-nowrap text-xl">
                                            {item.name}
                                          </span>

                                            </div>
                                        </Link>
                                    </li>

                                ))

                            }

                        </ul>

                        <button
                            className="bg-background text-foreground rounded-full p-4 flex items-center justify-center shadow-md hover:bg-highlight hover:scale-105 duration-200 cursor-pointer"
                            aria-label="Close"
                            onClick={() => setIsOpen(!isOpen)}
                        >
                            <IoMdClose size={32} />
                        </button>

                    </div>

                </nav>

            </div>

            { /* 4. Logout button, that should have a component itself */ }

            <button
                className="bg-background text-foreground rounded-full p-4 flex items-center justify-center shadow-md hover:bg-highlight hover:scale-105 duration-200 cursor-pointer"
                aria-label="Logout"
                type="button"
            >
                <IoLogOut size={32} />
            </button>

        </header>
    )

}