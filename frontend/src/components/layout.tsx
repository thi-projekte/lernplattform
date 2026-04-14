import {
    AppShell,
    Burger,
    Group,
    Text,
    NavLink,
} from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import {
    IconHammer,
} from '@tabler/icons-react';
import type {FC, ReactNode} from "react";

interface LayoutProps {
    children: ReactNode;
}

export const Layout: FC<LayoutProps> = ({children}) =>  {
    const [opened, { toggle }] = useDisclosure();

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
                        <Text fw={700} size="xl">ContentHub</Text>
                    </Group>
                </Group>
            </AppShell.Header>

            <AppShell.Navbar p="md">
                <NavLink
                    label="Builder Mode"
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