import {
    AppShell,
    Burger,
    Group, Image,
    NavLink,
} from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import {
    IconHammer,
} from '@tabler/icons-react';
import type {FC, ReactNode} from "react";
import LanguagePicker from "./language-picker.tsx";
import {useTranslation} from "react-i18next";

import logo from "../assets/logo.png"

interface LayoutProps {
    children: ReactNode;
}

export const Layout: FC<LayoutProps> = ({children}) =>  {
    const [opened, { toggle }] = useDisclosure();
    const {t} = useTranslation();

    return (
        <AppShell
            header={{ height: 60 }}
            navbar={{ width: 300, breakpoint: 'sm', collapsed: { mobile: !opened } }}
            padding="md"
        >
            <AppShell.Header>
                <Group h="100%" px="md" justify="space-between">
                    <Group>
                        <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" />
                        <Image
                            src={logo}
                            alt="MYnd Logo"
                            h={45}
                            w="auto"
                            fit="contain"
                        />
                    </Group>

                    <LanguagePicker />
                </Group>
            </AppShell.Header>

            <AppShell.Navbar p="md">
                <NavLink
                    label={t("common.builderMode")}
                    leftSection={<IconHammer size={16} stroke={1.5} />}
                    active
                />
            </AppShell.Navbar>

            <AppShell.Main>
                {children}
            </AppShell.Main>
        </AppShell>
    );
}