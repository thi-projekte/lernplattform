import type { ReactNode } from 'react';
import { Container, Loader } from '@mantine/core';

interface LoadingWrapperProps {
  children: ReactNode;
  isLoading: boolean;
}

const LoadingWrapper = ({ children, isLoading }: LoadingWrapperProps) => {
  if (isLoading) {
    return (
      <Container>
        <Loader size={50} />
      </Container>
    );
  }

  return children;
};

export default LoadingWrapper;
