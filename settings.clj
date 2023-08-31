;    Copyright (C) 2018  Joseph Fosco. All Rights Reserved
;
;    This program is free software: you can redistribute it and/or modify
;    it under the terms of the GNU General Public License as published by
;    the Free Software Foundation, either version 3 of the License, or
;    (at your option) any later version.
;
;    This program is distributed in the hope that it will be useful,
;    but WITHOUT ANY WARRANTY; without even the implied warranty of
;    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;    GNU General Public License for more details.
;
;    You should have received a copy of the GNU General Public License
;    along with this program.  If not, see <http://www.gnu.org/licenses/>.


{:log-level :info
 ;; ensemble density is printed as a info level log msg
 ;; print-ensemble-density manages when the ensemble density is logged
 ;;  :always     - prints ensemble density eachtime it is calculated
 ;;  : on-chenge - prints ensembly density only if the new calculated
 ;;                value is different than the previous value
 :print-ensemble-density :on-change  ;; :always or :on-change
 }
