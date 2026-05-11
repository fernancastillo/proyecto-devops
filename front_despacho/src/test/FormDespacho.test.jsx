import { render, screen } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import { FormDespacho } from '../componentes/CrudAdmin/FormDespacho'

// Mock de axios
vi.mock('axios', () => ({
  default: {
    put: vi.fn().mockResolvedValue({ data: {} }),
    post: vi.fn().mockResolvedValue({ data: {} })
  }
}))

// Mock de sweetalert2
vi.mock('sweetalert2', () => ({
  default: {
    fire: vi.fn()
  }
}))

const ventaMock = {
  idVenta: 1,
  direccionCompra: 'Av. Siempre Viva 742',
  valorCompra: 15000,
  despachoGenerado: false
}

describe('FormDespacho', () => {

  it('debe renderizar el título del formulario', () => {
    render(<FormDespacho venta={ventaMock} onClose={vi.fn()} />)
    expect(screen.getByText('Ingreso de orden de despacho')).toBeInTheDocument()
  })

  it('debe mostrar el campo de fecha de despacho', () => {
    render(<FormDespacho venta={ventaMock} onClose={vi.fn()} />)
    expect(screen.getByPlaceholderText('Ingresa fecha de despacho')).toBeInTheDocument()
  })

  it('debe mostrar el campo de patente de camión', () => {
    render(<FormDespacho venta={ventaMock} onClose={vi.fn()} />)
    expect(screen.getByPlaceholderText('Elige patente de camión')).toBeInTheDocument()
  })

  it('debe mostrar la dirección de la venta', () => {
    render(<FormDespacho venta={ventaMock} onClose={vi.fn()} />)
    expect(screen.getByDisplayValue('Av. Siempre Viva 742')).toBeInTheDocument()
  })

  it('debe mostrar el valor de compra de la venta', () => {
    render(<FormDespacho venta={ventaMock} onClose={vi.fn()} />)
    expect(screen.getByDisplayValue('15000')).toBeInTheDocument()
  })

  it('debe mostrar el botón de asignar despacho', () => {
    render(<FormDespacho venta={ventaMock} onClose={vi.fn()} />)
    expect(screen.getByRole('button', { name: /asignar despacho/i })).toBeInTheDocument()
  })

})