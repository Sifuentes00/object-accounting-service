import { useError } from '../context/ErrorContext';
import ErrorModal from './ErrorModal';

export default function ErrorDisplay() {
  const { error, clearError } = useError();

  return (
    <ErrorModal
      isOpen={error !== null}
      onClose={clearError}
      message={error || ''}
    />
  );
}
