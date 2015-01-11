package com.cube.nanotimer.gui.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.cube.nanotimer.Options;
import com.cube.nanotimer.R;
import com.cube.nanotimer.gui.widget.list.FieldDialog;
import com.cube.nanotimer.gui.widget.list.FieldEditDialog;
import com.cube.nanotimer.gui.widget.list.FieldRenamer;
import com.cube.nanotimer.util.helper.DialogUtils;
import com.mobeta.android.dslv.DragSortController;
import com.mobeta.android.dslv.DragSortListView;
import com.mobeta.android.dslv.DragSortListView.DropListener;

import java.util.ArrayList;
import java.util.List;

public class AddStepsDialog extends DialogFragment implements FieldRenamer {

  private int pos;
  private Dialog dialog;
  private StepsCreator stepsCreator;
  private List<String> liSteps;
  private StepsListAdapter stepsAdapter;
  private DragSortListView lvSteps;

  private static final int ACTION_RENAME = 0;
  private static final int ACTION_DELETE = 1;

  private static final String ARG_POS = "pos";

  public static AddStepsDialog newInstance(StepsCreator stepsCreator, int pos) {
    AddStepsDialog frag = new AddStepsDialog(stepsCreator);
    Bundle args = new Bundle();
    args.putInt(ARG_POS, pos);
    frag.setArguments(args);
    return frag;
  }

  private AddStepsDialog(StepsCreator stepsCreator) {
    this.stepsCreator = stepsCreator;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    pos = getArguments().getInt(ARG_POS);
    dialog = getCreationDialog();
    return dialog;
  }

  private Dialog getCreationDialog() {
    View view = getActivity().getLayoutInflater().inflate(R.layout.add_steps_dialog, null);
    final AlertDialog d = new AlertDialog.Builder(getActivity())
        .setView(view)
        .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
          }
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int whichButton) {
          }
        })
        .create();

    // Steps ListView
    liSteps = new ArrayList<String>();
    stepsAdapter = new StepsListAdapter(getActivity(), R.layout.simple_list_item, liSteps);
    lvSteps = (DragSortListView) view.findViewById(R.id.lvSteps);
    lvSteps.setAdapter(stepsAdapter);

    lvSteps.setDropListener(new DropListener() {
      @Override
      public void drop(int from, int to) {
        if (from != to) {
          String step = stepsAdapter.getItem(from);
          liSteps.remove(step);
          liSteps.add(to, step);
          stepsAdapter.notifyDataSetChanged();
        }
      }
    });

    DragSortController controller = new DragSortController(lvSteps);
    controller.setDragHandleId(R.id.imgMove);
    lvSteps.setFloatViewManager(controller);
    lvSteps.setOnTouchListener(controller);

    lvSteps.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        registerForContextMenu(lvSteps);
        getActivity().openContextMenu(view);
        unregisterForContextMenu(lvSteps);
      }
    });

    // Steps header
    final EditText tfStepName = (EditText) view.findViewById(R.id.tfStepName);
    Button buAddStep = (Button) view.findViewById(R.id.buAddStep);
    buAddStep.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        String stepName = tfStepName.getText().toString().trim();
        if (!"".equals(stepName.trim())) {
          if (liSteps.size() < Options.INSTANCE.getMaxStepsCount()) {
            if (!liSteps.contains(stepName)) {
              liSteps.add(stepName);
              tfStepName.setText("");
              stepsAdapter.notifyDataSetChanged();
              lvSteps.setSelection(liSteps.size() - 1); // scroll to bottom
            } else {
              DialogUtils.showInfoMessage(getActivity(), R.string.step_already_exists);
            }
          } else {
            DialogUtils.showInfoMessage(getActivity(), R.string.max_steps_count_reached);
          }
        }
      }
    });

    d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

    return d;
  }

  @Override
  public void onStart() {
    super.onStart();
    // redefining to avoid closing the dialog if we don't want to
    AlertDialog d = (AlertDialog) getDialog();
    if (d != null) {
      Button positiveButton = d.getButton(Dialog.BUTTON_POSITIVE);
      positiveButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          onConfirm();
        }
      });
    }
  }

  private void onConfirm() {
    if (liSteps.size() < 2) {
      DialogUtils.showInfoMessage(getActivity(), R.string.min_steps_count_not_reached);
    } else {
      stepsCreator.addSteps(liSteps, pos);
      dismiss();
    }
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    if (v.getId() == R.id.lvSteps) {
      menu.setHeaderTitle(R.string.action);
      menu.add(v.getId(), ACTION_RENAME, 0, R.string.rename);
      menu.add(v.getId(), ACTION_DELETE, 0, R.string.delete);
      for (int i = 0; i < menu.size(); i++) {
        menu.getItem(i).setOnMenuItemClickListener(new OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem menuItem) {
            AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuItem.getMenuInfo();
            switch (menuItem.getItemId()) {
              case ACTION_RENAME:
                FieldDialog fieldDialog = FieldEditDialog.newInstance(AddStepsDialog.this, info.position, liSteps.get(info.position));
                DialogUtils.showFragment(getActivity(), fieldDialog);
                break;
              case ACTION_DELETE:
                liSteps.remove(info.position);
                stepsAdapter.notifyDataSetChanged();
                break;
            }
            return true;
          }
        });
      }
    }
  }

  @Override
  public boolean renameField(int index, String newName) {
    if ("".equals(newName.trim())) {
      return false;
    }
    if (!liSteps.contains(newName)) {
      liSteps.set(index, newName);
      stepsAdapter.notifyDataSetChanged();
      lvSteps.setSelection(index);
      return true;
    } else {
      if (liSteps.indexOf(newName) != index) {
        DialogUtils.showInfoMessage(getActivity(), R.string.step_already_exists);
        return false;
      } else {
        return true; // did not change
      }
    }
  }

  @Override
  public void show(FragmentManager manager, String tag) {
    if (manager.findFragmentByTag(tag) == null) {
      super.show(manager, tag);
    }
  }

  private class StepsListAdapter extends ArrayAdapter<String> {
    public StepsListAdapter(Context context, int id, List<String> list) {
      super(context, id, list);
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
      View view = convertView;
      if (view == null) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.steps_list_item, null);
      }

      if (position >= 0 && position < liSteps.size()) {
        String item = liSteps.get(position);
        if (item != null) {
          TextView tvName = (TextView) view.findViewById(R.id.tvStepName);
          tvName.setText(item);
        }
      }
      return view;
    }
  }

}
