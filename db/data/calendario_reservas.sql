-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Servidor: localhost
-- Tiempo de generación: 01-10-2025 a las 04:49:53
-- Versión del servidor: 10.4.28-MariaDB
-- Versión de PHP: 8.2.4
USE `calendario_reservas`;
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de datos: `calendario_reservas`
--

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `cliente`
--

CREATE TABLE `cliente` (
  `id_cliente` bigint(20) NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `telefono` varchar(15) DEFAULT NULL,
  `medio` bigint(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `cliente`
--

INSERT INTO `cliente` (`id_cliente`, `nombre`, `email`, `telefono`, `medio`) VALUES
(2, 'leonel', 'leonel_@hotmla.com', '21343207', 0),
(3, 'leo', 'leo.lto@gmail.com', '12324', 1);

-- --------------------------------------------------------

--
-- Estructura de tabla para la tabla `reserva`
--

CREATE TABLE `reserva` (
  `id_reserva` bigint(20) NOT NULL,
  `id_cliente` bigint(20) NOT NULL,
  `fecha_reserva` datetime NOT NULL,
  `fecha_termino` datetime DEFAULT NULL,
  `precio` bigint(20) NOT NULL DEFAULT 0,
  `estado` varchar(20) NOT NULL,
  `fecha_creacion` datetime NOT NULL DEFAULT current_timestamp(),
  `fecha_modificacion` datetime NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  `lugar_encuentro` text DEFAULT NULL,
  `mensaje_personalizado` text DEFAULT NULL,
  `nombre_producto` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Volcado de datos para la tabla `reserva`
--

INSERT INTO `reserva` (`id_reserva`, `id_cliente`, `fecha_reserva`, `fecha_termino`, `precio`, `estado`, `fecha_creacion`, `fecha_modificacion`, `lugar_encuentro`, `mensaje_personalizado`, `nombre_producto`) VALUES
(3, 2, '2025-09-19 03:01:58', NULL, 23222, 'PENDIENTE', '2025-09-03 00:55:59', '2025-09-03 00:55:59', 'metro el bosque', 'aaaaaa', 'snoopy'),
(4, 3, '2025-09-11 04:20:21', NULL, 2999, 'ABONADA', '2025-09-03 01:24:24', '2025-09-03 01:24:24', 'el bosque', 'dsjhfoisueiorujskldjvlsc', 'leito jr'),
(5, 3, '2025-09-12 04:20:21', NULL, 2999, 'CANCELADA', '2025-09-03 01:25:11', '2025-09-03 01:25:11', 'el bosque', 'dsjhfoisueiorujskldjvlsc', 'leito jr'),
(6, 3, '2025-09-11 04:20:21', NULL, 2999, 'ABONADA', '2025-09-03 01:25:11', '2025-09-03 01:25:11', 'el bosque', 'dsjhfoisueiorujskldjvlsc', 'leito jr'),
(7, 3, '2025-09-19 03:03:47', NULL, 1232120, 'PAGADA', '2025-09-18 00:04:29', '2025-09-18 00:04:29', 'si', 'asdasfwewr32rwe', 'ooooo');

--
-- Índices para tablas volcadas
--

--
-- Indices de la tabla `cliente`
--
ALTER TABLE `cliente`
  ADD PRIMARY KEY (`id_cliente`),
  ADD UNIQUE KEY `email` (`email`),
  ADD KEY `idx_cliente_nombre` (`nombre`),
  ADD KEY `idx_cliente_email` (`email`);

--
-- Indices de la tabla `reserva`
--
ALTER TABLE `reserva`
  ADD PRIMARY KEY (`id_reserva`),
  ADD KEY `fk_reserva_cliente` (`id_cliente`);

--
-- AUTO_INCREMENT de las tablas volcadas
--

--
-- AUTO_INCREMENT de la tabla `cliente`
--
ALTER TABLE `cliente`
  MODIFY `id_cliente` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT de la tabla `reserva`
--
ALTER TABLE `reserva`
  MODIFY `id_reserva` bigint(20) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- Restricciones para tablas volcadas
--

--
-- Filtros para la tabla `reserva`
--
ALTER TABLE `reserva`
  ADD CONSTRAINT `fk_reserva_cliente` FOREIGN KEY (`id_cliente`) REFERENCES `cliente` (`id_cliente`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
