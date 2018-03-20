function [ wave ] = transmit(f, A, omega, t, sampling_rate)
%% transmit: Given a amplitude, frequency, phase, 
%  transmit(A, f, t, sampling_rate) will generate a acoustic wave
%  for t seconds. 

    if nargin < 5   sampling_rate = 44100;  end;
    if nargin < 4   t = 1;                  end;
    if nargin < 3   omega = 0;              end;
    if nargin < 2   A = 1;                  end;
    if nargin < 1   f = 1000;               end;
	dur = (1: t*sampling_rate)/sampling_rate;
    wave = A*sin(2*pi*f*dur + omega); 
	sound(wave, sampling_rate);

end
