import type { Metadata } from "next";
import "./globals.css";
import React from "react";
import {Roboto} from "next/font/google";
import Header from "@/components/Header";
import {IoMdHome} from "react-icons/io";
import {PiCertificateFill} from "react-icons/pi";

export const metadata: Metadata = {
  title: "MyTracker",
  description: "MyTracker - Track your vehicles easily",
};

const roboto = Roboto({
    subsets: ["latin"],
    weight: ["400", "700"],
})

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {

    const items = [

        {
            name: "Início",
            icon: <IoMdHome size={24} />
        },
        {
            name: "Certificados",
            icon: <PiCertificateFill size={24} />
        }

    ]

  return (
    <html lang="en">
      <body className={`${roboto.className} antialiased flex`}>
        <Header items={items} />
        {children}
      </body>
    </html>
  );

}
