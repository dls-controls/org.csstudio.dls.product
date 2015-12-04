#!/dls_sw/prod/R3.14.12.3/support/pythonSoftIoc/2-6/pythonIoc

import pkg_resources
pkg_resources.require('numpy')
pkg_resources.require('cothread')
pkg_resources.require('iocbuilder')

import numpy
import softioc
from softioc import builder
import cothread


# The time in seconds to pause between each PV update
DYNAMIC_PV_UPDATE_DELAY = 1.0

# Values passed to the Db constructor for each type
ALARM_SEVERITIES = {
        'HHSV':'MAJOR', 'HSV':'MINOR', 'LSV': 'MINOR', 'LLSV': 'MAJOR'}
ANALOGUE_VALUES = dict(
        {'HOPR': 10, 'HIHI': 9, 'HIGH': 7, 'LOW': 3, 'LOLO': 1, 'LOPR':0,
            'initial_value': 5},
        **ALARM_SEVERITIES)
LONG_VALUES = ANALOGUE_VALUES.copy()
BOOLEAN_VALUES = {'ZNAM': 'FALSE', 'ONAM': 'TRUE', 'initial_value': 0}

# List of strings for use with Enum PVs
STRINGS = ['one', 'two', 'three', 'four']


def increment(pv, wrap):
    """
    Increments the value in the given PV up to and including
    the value of wrap. Incrementing the value of the PV past wrap
    will reset the value to zero.
    """
    value = pv._RecordWrapper__device.get()
    new_value = (value + 1) % (wrap + 1)
    pv._RecordWrapper__device.set(new_value)


def update_string(pv):
    """
    Updates the value of a string PV to the next value
    in the global variable STRINGS.
    """
    value = pv._RecordWrapper__device.get()
    try:
        new_index = (STRINGS.index(value) + 1) % len(STRINGS)
    except ValueError: # Someone changed the PV to an unknown value
        new_index = 0
    pv._RecordWrapper__device.set(STRINGS[new_index])


def update_pvs(pv_dict):
    """
    Cycles through all PVs in the dict, and calls the corresponding update
    function associated with the PV, passing the PV as the only argument.
    """
    while True:
        cothread.Sleep(DYNAMIC_PV_UPDATE_DELAY)
        for pair in pv_dict.items():
            pair[1](pair[0])


def build_typed_pvs():
    """Build generic PVs"""
    dynamic_pvs = {}

    builder.aOut('AO:STATIC', **ANALOGUE_VALUES)
    builder.aIn('AI:STATIC', **ANALOGUE_VALUES)
    dynamic_pvs.update({
        builder.aOut('AO:DYNAMIC', **ANALOGUE_VALUES):
        lambda pv: incremeant(pv, 10)})

    builder.longOut('LONGO:STATIC',**LONG_VALUES)
    builder.longIn('LONGI:STATIC',**LONG_VALUES)
    dynamic_pvs.update({
        builder.aOut('LONGO:DYNAMIC', **LONG_VALUES):
        lambda pv: incremeant(pv, 10)})

    builder.boolOut('BOOLO:STATIC', **BOOLEAN_VALUES)
    builder.boolIn('BOOLI:STATIC', **BOOLEAN_VALUES)
    dynamic_pvs.update({
        builder.boolOut('BOOLO:DYNAMIC', **BOOLEAN_VALUES):
        lambda pv: incremeant(pv, 1)})

    builder.stringOut('STRINGO:STATIC', initial_value=STRINGS[0])
    builder.stringIn('STRINGI:STATIC', initial_value=STRINGS[0])
    dynamic_pvs.update({
        builder.stringOut('STRINGO:DYNAMIC', initial_value=STRINGS[0]):
        update_string})

    enum_pv = builder.mbbOut('MBBO:STATIC', *STRINGS, initial_value=0)
    enum_pv = builder.mbbIn('MBBI:STATIC', *STRINGS, initial_value=0)
    dynamic_pvs.update({
        builder.mbbOut('MBBO:DYNAMIC', *STRINGS, initial_value=0):
        lambda pv: incremeant(pv, len(STRINGS) - 1)})
    return lambda: update_pvs(dynamic_pvs)


def build_interactive_sin():
    """
    Set up an interactive sine wave that has controls attached to PVs.
    Two output waves our generated, one which samples the sine function
    at a fixed update rate, and the other which outputs a single cycle
    of a sine wave in a waveform PV.

    This returns a function that is intended to be passed to Cothread's
    Spawn function to provide the updates of the travelling wave.
    """

    class Sin(object):
        """
        Holds the state required to simulate a sine wave with external
        PV controls.
        """
        NUM_POINTS = 200
        def __init__(self):
            self.amp = 1
            self.phase = 0
            self.wf_pv = None
            self.travel_pv = None
            self.x = numpy.linspace(0, 2*numpy.pi, self.NUM_POINTS)
            self._update_waveform()
        def set_phase(self, phase):
            self.phase = phase
            self._update_waveform()
        def set_amp(self, amp):
            self.amp = amp
            self._update_waveform()
        def register_waveform_pv(self, pv):
            self.wf_pv = pv
        def register_travelling_pv(self, pv):
            self.travel_pv = pv
        def get_travelling_update_function(self):
            UPDATE_RATE = 10
            TICK_DELTA = numpy.pi / UPDATE_RATE
            X = 0
            def run_sin_wave():
                x = X
                while True:
                    cothread.Sleep(1./UPDATE_RATE)
                    x += TICK_DELTA
                    value = self.amp * numpy.sin(x + self.phase)
                    if self.travel_pv:
                        self.travel_pv._RecordWrapper__device.set(value)
            return run_sin_wave
        def _update_waveform(self):
            self.wf = self.amp * numpy.sin(self.x + self.phase)
            if self.wf_pv:
                self.wf_pv._RecordWrapper__device.set(self.wf)

    sin = Sin()
    builder.WaveformOut('SINE:X', sin.x)
    builder.aOut(
            'SINE:AMP', on_update=lambda x: sin.set_amp(x),
            LOPR=0, HOPR=2, PREC=2, initial_value=sin.amp)
    builder.aOut(
            'SINE:PHASE', on_update=lambda x: sin.set_phase(x),
            LOPR=0, HOPR=numpy.pi*2, PREC=2, initial_value=sin.phase)
    sin.register_travelling_pv(
            builder.aOut('SINE:TRAVEL', PREC=2, initial_value=0))
    sin.register_waveform_pv(builder.WaveformOut('SINE:WF', sin.wf))

    return sin.get_travelling_update_function()


builder.SetDeviceName('TS-TS-IOC-01')
spawnables = [build_typed_pvs(), build_interactive_sin()]

builder.LoadDatabase()
softioc.softioc.iocInit()

for spawnable in spawnables:
    cothread.Spawn(spawnable)
softioc.softioc.interactive_ioc(globals())

