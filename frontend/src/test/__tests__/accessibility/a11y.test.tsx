import { describe, it, expect } from 'vitest';
import { render } from '@testing-library/react';
import EmptyState from '@/shared/components/EmptyState/EmptyState';
import PeopleIcon from '@mui/icons-material/People';

// Basic a11y structural checks (axe requires DOM environment adjustments with jsdom)
describe('Accessibility — EmptyState', () => {
  it('renders with accessible title', () => {
    const { getByText } = render(
      <EmptyState icon={<PeopleIcon />} title="No users found" description="Add a user to get started" action={{ label:'Add user', onClick:() => {} }} />
    );
    expect(getByText('No users found')).toBeTruthy();
    expect(getByText('Add a user to get started')).toBeTruthy();
    expect(getByText('Add user')).toBeTruthy();
  });

  it('renders without action', () => {
    const { getByText } = render(<EmptyState icon={<PeopleIcon />} title="Empty" />);
    expect(getByText('Empty')).toBeTruthy();
  });
});
