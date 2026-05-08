import {
  useReactTable,
  getCoreRowModel,
  type RowData,
  type PaginationState,
  type TableOptions,
  flexRender,
} from '@tanstack/react-table';
import { Alert, Box, Group, Pagination, ScrollArea, Select, Table, Text } from '@mantine/core';
import { useTranslation } from 'react-i18next';

export interface EntityTableProps<T> {
  data: T[];
  columns: TableOptions<T>['columns'];
  pageCount?: number;
  pagination?: PaginationState;
  setPagination?: (state: PaginationState) => void;
  isFetching?: boolean;
  hidePagination?: boolean;
}

function EntityTable<T extends RowData>({
  data,
  columns,
  pageCount,
  pagination,
  setPagination,
  isFetching,
  hidePagination,
}: EntityTableProps<T>) {
  // eslint-disable-next-line react-hooks/incompatible-library
  const table = useReactTable({
    getCoreRowModel: getCoreRowModel(),
    data,
    columns,
    pageCount,
    state: { pagination },
    onPaginationChange: (update) => setPagination?.(update as PaginationState),
    manualPagination: true,
  });

  const { t } = useTranslation();

  return (
    <Box p="md">
      <ScrollArea>
        <Table
          highlightOnHover
          withTableBorder
          style={{
            opacity: isFetching ? 0.1 : 1,
            transition: 'opacity 0.2s ease',
          }}
        >
          <Table.Thead>
            {table.getHeaderGroups().map((headerGroup) => (
              <Table.Tr key={headerGroup.id}>
                {headerGroup.headers.map((header) => (
                  <Table.Th key={header.id}>
                    {flexRender(header.column.columnDef.header, header.getContext())}
                  </Table.Th>
                ))}
              </Table.Tr>
            ))}
          </Table.Thead>
          <Table.Tbody>
            {table.getRowModel().rows.map((row) => (
              <Table.Tr key={row.id}>
                {row.getVisibleCells().map((cell) => (
                  <Table.Td key={cell.id}>
                    {flexRender(cell.column.columnDef.cell, cell.getContext())}
                  </Table.Td>
                ))}
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </ScrollArea>

      {data.length === 0 && !isFetching && (
        <Alert color="yellow" mt={12}>
          {t('common.noEntriesFound')}
        </Alert>
      )}

      {!hidePagination && pagination && pageCount && (
        <Group justify="space-between" mt="md">
          <Group gap="xs">
            <Text size="sm" c="dimmed">
              {t('common.rowsPerPage')}:
            </Text>
            <Select
              size="xs"
              w={80}
              data={['10', '20', '50']}
              value={pagination?.pageSize.toString()}
              onChange={(val) => table.setPageSize(Number(val))}
            />
          </Group>
          <Pagination
            total={pageCount}
            value={pagination.pageIndex + 1}
            onChange={(page) => table.setPageIndex(page - 1)}
            withEdges
            size="sm"
            disabled={isFetching}
          />
        </Group>
      )}
    </Box>
  );
}

export default EntityTable;
