import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import axe from 'axe-core';
import RowActionsMenu from '@/shared/components/RowActionsMenu';
import ServerDataTable from '@/shared/components/AdvancedDataTable/ServerDataTable';

/**
 * Keyboard and screen-reader contracts for the two shared table primitives:
 * the row menu is fully operable without a mouse, and the table exposes sort
 * state, labelled controls and no axe violations.
 */
describe('RowActionsMenu keyboard access', () => {
  it('opens with Enter, moves with arrows, activates with Enter, closes with Escape', async () => {
    const edit = vi.fn(); const remove = vi.fn();
    const user = userEvent.setup();
    render(<RowActionsMenu actions={[
      { label: 'Edit', icon: <span />, onClick: edit },
      { label: 'Delete', icon: <span />, onClick: remove, color: 'error' },
    ]} />);

    await user.tab();
    expect(screen.getByRole('button', { name: /more actions/i })).toHaveFocus();
    await user.keyboard('{Enter}');
    expect(await screen.findByRole('menu')).toBeInTheDocument();
    await user.keyboard('{ArrowDown}');
    expect(screen.getByRole('menuitem', { name: 'Delete' })).toHaveFocus();
    await user.keyboard('{Enter}');
    expect(remove).toHaveBeenCalledTimes(1);
    expect(edit).not.toHaveBeenCalled();

    await user.keyboard('{Enter}');
    await screen.findByRole('menu');
    await user.keyboard('{Escape}');
    await vi.waitFor(() => expect(screen.queryByRole('menu')).not.toBeInTheDocument());
  });
});

describe('ServerDataTable accessibility', () => {
  const rows = [{ id: '1', name: 'Alpha' }, { id: '2', name: 'Beta' }];
  const columns = [{ field: 'name' as const, headerName: 'Name', width: 200 }];
  const noop = () => {};

  it('exposes sort state, labelled controls and a keyboard-reachable row action', async () => {
    const onRowClick = vi.fn();
    const user = userEvent.setup();
    render(
      <ServerDataTable
        title="Widgets" rows={rows} columns={columns}
        pagination={{ page: 0, size: 20, totalElements: 2, totalPages: 1, first: true, last: true }}
        page={0} pageSize={20} onPageChange={noop} onPageSizeChange={noop}
        sort={{ field: 'name', direction: 'desc' }} onSortChange={noop}
        search="" onSearchChange={noop} rowKey={(r) => r.id} onRowClick={onRowClick} onRefetch={noop}
      />,
    );

    expect(screen.getByRole('columnheader', { name: /name/i })).toHaveAttribute('aria-sort', 'descending');
    expect(screen.getByLabelText('Search')).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Refresh' })).toBeInTheDocument();

    const row = screen.getByRole('cell', { name: 'Alpha' }).closest('tr')!;
    row.focus();
    await user.keyboard('{Enter}');
    expect(onRowClick).toHaveBeenCalledWith(rows[0]);
  });

  it('has no axe violations', async () => {
    const { container } = render(
      <ServerDataTable
        title="Widgets" rows={rows} columns={columns}
        pagination={{ page: 0, size: 20, totalElements: 2, totalPages: 1, first: true, last: true }}
        page={0} pageSize={20} onPageChange={noop} onPageSizeChange={noop}
        sort={null} onSortChange={noop} search="" onSearchChange={noop} rowKey={(r) => r.id}
      />,
    );
    const results = await axe.run(container, { rules: { 'color-contrast': { enabled: false } } }); // jsdom cannot compute contrast
    expect(results.violations.map((v) => `${v.id}: ${v.nodes.map((n) => n.target.join(' ')).join(', ')}`)).toEqual([]);
  });
});
